package com.felix.livinglink.server.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class NamingConventionsTest {
    @Test
    fun `top-level classes in application packages end with 'UseCase'`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("..application..") }
            .filter { it.isTopLevel }
            .assertTrue { it.name.endsWith("UseCase") }
    }

    @Test
    fun `top-level interfaces in domain packages that are repositories end with 'Repository'`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .filter { it.resideInPackage("..domain..") }
            .filter { it.isTopLevel }
            .filter { it.name.contains("Repository") }
            .assertTrue { it.name.endsWith("Repository") }
    }

    @Test
    fun `top-level MCP tool registrars in tools packages end with 'Tool'`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("..delivery.mcp.tools..") }
            .filter { it.isTopLevel }
            .assertTrue { it.name.endsWith("Tool") }
    }

    @Test
    fun `top-level MCP DTOs in dto packages end with 'McpDto'`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .filter { it.resideInPackage("..delivery.mcp.dto..") }
            .filter { it.isTopLevel }
            .assertTrue { it.name.endsWith("McpDto") }
    }

    @Test
    fun `top-level MCP DTO interfaces in dto packages end with 'McpDto'`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .filter { it.resideInPackage("..delivery.mcp.dto..") }
            .filter { it.isTopLevel }
            .assertTrue { it.name.endsWith("McpDto") }
    }
}
