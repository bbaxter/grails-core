import org.hibernate.dialect.DialectFactory
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.support.JdbcUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << grailsScript("_GrailsPackage")

def props = new Properties()
def filename = "${grailsSettings.projectTargetDir}/ddl.sql"
boolean export = false
boolean stdout = false
String configClassName = 'org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration'

def configClasspath = {

    ant.copy(todir: classesDirPath, file: metadataFile)
    ant.copy(todir: classesDirPath, failonerror: false) {
        fileset(dir: "${basedir}/grails-app/conf", excludes: '*.groovy, log4j*, hibernate, spring')
        fileset(dir: "${basedir}/grails-app/conf/hibernate")
        fileset(dir: "${basedir}/src/java", excludes: '**/*.java')
    }
}

def configureFromArgs = {
    args = args ?: ''
    args.split('\n').each {arg ->
        arg = arg.trim()
        if (arg) {
            switch(arg) {
                case 'export': export = true; break
                case 'generate': export = false; break
                case 'stdout': stdout = true; break
                default: filename = arg
            }
        }
    }
}

def populateProperties = {

    createConfig()
    def dsConfig = CH.config

    props.'hibernate.connection.username' = dsConfig?.dataSource?.username ?: 'sa'
    props.'hibernate.connection.password' = dsConfig?.dataSource?.password ?: ''
    props.'hibernate.connection.url' = dsConfig?.dataSource?.url ?: 'jdbc:hsqldb:mem:testDB'
    props.'hibernate.connection.driver_class' =
            dsConfig?.dataSource?.driverClassName ?: 'org.hsqldb.jdbcDriver'

    if (dsConfig?.dataSource?.configClass) {
        if (dsConfig.dataSource.configClass instanceof Class) {
            configClassName = dsConfig.dataSource.configClass.name
        }
        else {
            configClassName = dsConfig.dataSource.configClass
        }
    }

    if (dsConfig?.dataSource?.dialect) {
        def dialect = dsConfig.dataSource.dialect
        if (dialect instanceof Class) {
            dialect = dialect.name
        }
        props.'hibernate.dialect' = dialect
    }
    else {
        println('WARNING: Autodetecting the Hibernate Dialect; consider specifying the class name in DataSource.groovy')
        try {
            def ds = new DriverManagerDataSource(
                    props.'hibernate.connection.driver_class',
                    props.'hibernate.connection.url',
                    props.'hibernate.connection.username',
                    props.'hibernate.connection.password')
            def dbName = JdbcUtils.extractDatabaseMetaData(ds, 'getDatabaseProductName')
            def majorVersion = JdbcUtils.extractDatabaseMetaData(ds, 'getDatabaseMajorVersion')
            props.'hibernate.dialect' =
                DialectFactory.determineDialect(dbName, majorVersion).class.name
        }
        catch (Exception e) {
            println "ERROR: Problem autodetecting the Hibernate Dialect: ${e.message}"
            throw e
        }
    }
}

target(schemaExport: 'Run Hibernate SchemaExport') {
    depends(packageApp)

    configureFromArgs()

    def file = new File(filename)
    ant.mkdir(dir: file.parentFile)

    configClasspath()
    loadApp()

    populateProperties()

    def configuration = classLoader.loadClass(configClassName).newInstance()
    configuration.setGrailsApplication(grailsApp)
    configuration.setProperties(props)
    def hibernateCfgXml = eventsClassLoader.getResource('hibernate/hibernate.cfg.xml')
    if (hibernateCfgXml) {
        configuration.configure(hibernateCfgXml)
    }

    def schemaExport = classLoader.loadClass('org.hibernate.tool.hbm2ddl.SchemaExport')
        .newInstance(configuration)
        .setHaltOnError(true)
        .setOutputFile(file.path)
        .setDelimiter(';')

    def action = export ? "Exporting" : "Generating script to ${file.path}"
    println "${action} in environment '${grailsEnv}' using properties ${props}"

    if (export) {
        // 1st drop, warning exceptions
        schemaExport.execute(stdout, true, true, false)
        schemaExport.exceptions.clear()
        // then create
        schemaExport.execute(stdout, true, false, true)
    }
    else {
        // generate
        schemaExport.execute(stdout, false, false, false)
    }

    if (!schemaExport.exceptions.empty) {
        schemaExport.exceptions[0].printStackTrace()
    }
}

setDefaultTarget(schemaExport)
