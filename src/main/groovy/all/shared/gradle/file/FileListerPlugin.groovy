//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class FileListerPlugin implements Plugin<Project> {
  void apply(final Project project) {
    project.extensions.create('fileLister', FileLister, project)
  }
}
