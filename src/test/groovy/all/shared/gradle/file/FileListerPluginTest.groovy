//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtensionContainer

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify

@CompileStatic
class FileListerPluginTest {
  private final Project mockProject = mock(Project)
  private final ExtensionContainer mockExtensions = mock(ExtensionContainer)
  private final Logger mockLogger = mock(Logger)

  @BeforeEach
  void beforeTest() {
    doReturn(mockExtensions)
      .when(mockProject)
      .getExtensions()
    doReturn(mockLogger)
      .when(mockProject)
      .getLogger()
  }

  @Test
  void shouldComplementPlugin() {
    final boolean result = FileListerPlugin.complement(mockProject)

    assertTrue(result)
    verify(mockExtensions)
      .create(eq(FileListerPlugin.EXTENSION_NAME), eq(FileListerExtension), eq(mockProject))
    verify(mockLogger)
      .debug(eq('Added fileLister extension'))
  }

  @Test
  void shouldNotComplementPlugin() {
    doReturn(new Object())
      .when(mockExtensions)
      .findByName(FileListerPlugin.EXTENSION_NAME)

    final boolean result = FileListerPlugin.complement(mockProject)

    assertFalse(result)
    verify(mockExtensions, never())
      .create(eq(FileListerPlugin.EXTENSION_NAME), eq(FileListerExtension), eq(mockProject))
    verify(mockLogger)
      .error(eq('Couldn\'t add fileLister extension'))
  }

  @Test
  void shouldApplyPlugin() {
    final FileListerPlugin fileLister = new FileListerPlugin()

    fileLister.apply(mockProject)

    verify(mockExtensions)
      .create(eq(FileListerPlugin.EXTENSION_NAME), eq(FileListerExtension), eq(mockProject))
    verify(mockLogger)
      .debug(eq('Added fileLister extension'))
  }
}
