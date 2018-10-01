//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

import org.junit.jupiter.api.Test

import static org.mockito.Matchers.eq
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

@CompileStatic
class FileListerPluginTest {
  @Test
  void shouldApplyPlugin() {
    final FileListerPlugin fileLister = new FileListerPlugin()
    final Project mockProject = mock(Project)
    final ExtensionContainer mockExtensions = mock(ExtensionContainer)
    doReturn(mockExtensions)
      .when(mockProject)
      .getExtensions()

    fileLister.apply(mockProject)

    verify(mockExtensions).create(eq('fileLister'), eq(FileLister), eq(mockProject))
  }
}
