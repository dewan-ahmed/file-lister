//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class FileListerPlugin implements Plugin<Project> {
  static final String EXTENSION_NAME = 'fileLister'

  static final boolean complement(final Project project) {
    if (project.extensions.findByName(EXTENSION_NAME) == null) {
      project.extensions.create(EXTENSION_NAME, FileListerExtension, project)
      project.logger.debug('Added file-lister extension')
      true
    }
    else {
      project.logger.error('Couldn\'t add file-lister extension')
      false
    }
  }

  void apply(final Project project) {
    complement(project)
  }
}
