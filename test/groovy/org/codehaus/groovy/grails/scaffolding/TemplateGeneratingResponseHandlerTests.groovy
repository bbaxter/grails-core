package org.codehaus.groovy.grails.scaffolding

import grails.util.*
import org.springframework.web.context.request.*
import org.springframework.mock.web.*
import org.springframework.core.io.*
import org.springframework.web.context.request.*
import org.codehaus.groovy.grails.web.servlet.mvc.*
import org.codehaus.groovy.grails.web.errors.*
import org.codehaus.groovy.grails.web.pages.*
import org.codehaus.groovy.grails.support.*
import org.codehaus.groovy.grails.commons.*


class TemplateGeneratingResponseHandlerTests extends GroovyTestCase {

    def application
    
    void setUp() {
        application = new DefaultGrailsApplication([Test.class] as Class[], new GroovyClassLoader())
    }
    void testCreateScaffoldedListResponse() {
        def webRequest = GrailsWebUtil.bindMockWebRequest()
        webRequest.actionName = "list"

        def url = "/test/list"
        def handler = new TemplateGeneratingResponseHandler()
        handler.templateGenerator = new DefaultGrailsTemplateGenerator()
        handler.templateEngine = new GroovyPagesTemplateEngine(webRequest.servletContext)
        handler.scaffoldedClass = Test.class
        handler.grailsApplication = application

        def mv = handler.createScaffoldedResponse(url, [tests:[Test.newInstance()]])

        assert mv
        assert mv.model.tests
        assert mv.view instanceof ScaffoldedGroovyPageView        
    }

    void testCreateScaffoldedShowResponse() {
        def webRequest = GrailsWebUtil.bindMockWebRequest()
        webRequest.actionName = "show"

        def url = "/test/show"
        def handler = new TemplateGeneratingResponseHandler()
        handler.templateGenerator = new DefaultGrailsTemplateGenerator()
        handler.templateEngine = new GroovyPagesTemplateEngine(webRequest.servletContext)
        handler.scaffoldedClass = Test.class
        handler.grailsApplication = application

        def mv = handler.createScaffoldedResponse(url, [test:[Test.newInstance()]])

        assert mv
        assert mv.model.test
        assert mv.view instanceof ScaffoldedGroovyPageView
    }

    void testCreateScaffoldedEditResponse() {
        def webRequest = GrailsWebUtil.bindMockWebRequest()
        webRequest.actionName = "edit"

        def url = "/test/edit"
        def handler = new TemplateGeneratingResponseHandler()
        handler.templateGenerator = new DefaultGrailsTemplateGenerator()
        handler.templateEngine = new GroovyPagesTemplateEngine(webRequest.servletContext)
        handler.scaffoldedClass = Test.class
        handler.grailsApplication = application

        def mv = handler.createScaffoldedResponse(url, [test:[Test.newInstance()]])

        assert mv
        assert mv.model.test
        assert mv.view instanceof ScaffoldedGroovyPageView
    }

    void testCreateScaffoldedCreateResponse() {
        def webRequest = GrailsWebUtil.bindMockWebRequest()
        webRequest.actionName = "create"

        def url = "/test/create"
        def handler = new TemplateGeneratingResponseHandler()
        handler.templateGenerator = new DefaultGrailsTemplateGenerator()
        handler.templateEngine = new GroovyPagesTemplateEngine(webRequest.servletContext)
        handler.scaffoldedClass = Test.class
        handler.grailsApplication = application

        def mv = handler.createScaffoldedResponse(url, [test:[Test.newInstance()]])

        assert mv
        assert mv.model.test
        assert mv.view instanceof ScaffoldedGroovyPageView
    }

    void tearDown() {
         RequestContextHolder.setRequestAttributes(null)
    }
}
class Test {
    Long id
    Long version
    String name
}