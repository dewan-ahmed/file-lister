//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTreeElement

@CompileStatic
class FileListerExtension {
  private final static Iterable<String> EMPTY_ITERABLE = []
  private final static Iterable<String> DEFAULT_EXCLUDES = [
    '**/gradle-wrapper.jar',
    '**/.gradle'
  ]
  private final Project project

  FileListerExtension (final Project project) {
    this.project = project
  }

  ConfigurableFileTree obtainFullFileTree(final String folder = '.', final Map<String, List<String>> cludes = [:]) {
    return showDebugInfo(obtainFileTree(folder, cludes?.excludes ?: EMPTY_ITERABLE, cludes?.includes ?: EMPTY_ITERABLE))
  }

  ConfigurableFileTree obtainPartialFileTree(final String folder = '.', final Map<String, List<String>> cludes = [:]) {
    final ConfigurableFileTree tree = obtainFullFileTree(folder, cludes)
    return showDebugInfo((ConfigurableFileTree) tree.exclude(obtainAllExcludes(folder, tree)))
  }

  private static Iterable<String> obtainExcludesFromGitIgnore(final File gitIgnoreFile) {
    return gitIgnoreFile
      .readLines()
      *.trim()
      .findAll { !it.empty && !it.matches('(^\\s*[#].*)|(.*[\\[\\]\\!].*)') } // Ignores patterns with ! [ ] and comments
      .collect {
        it.matches('(^[^/].*/?$)')
          ? "/**/$it"
          : it
      }
      .collect { gitIgnoreFile.parent + it }
  }

  private Iterable<String> obtainExcludesFromDir(final String dir) {
    final File gitIgnoreFile = project.file("$dir/.gitignore")
    project.logger.debug "Scanning $gitIgnoreFile for ignored patterns"
    return gitIgnoreFile.exists()
      ? obtainExcludesFromGitIgnore(gitIgnoreFile)
      : [] as Iterable<String>
  }

  private Iterable<String> removeDirPathToExcludes(final Iterable<String> excludes, final String dirPath) {
    return excludes.asList()
      .collect { (it - dirPath) - '/' }
  }

  private Iterable<String> obtainAllExcludes(final String folder, final ConfigurableFileTree tree) {
    Iterable<String> allExcludes = obtainExcludesFromDir(folder)
    tree.visit { FileTreeElement el ->
      if (el.directory) {
        allExcludes += obtainExcludesFromDir(el.file.path)
      }
    }
    return removeDirPathToExcludes(allExcludes, tree.dir.path)
  }

  private ConfigurableFileTree showDebugInfo(final ConfigurableFileTree tree) {
    if (project.logger.debugEnabled) {
      tree.includes.each { project.logger.debug "Including $it in $tree.dir" }
      tree.excludes.each { project.logger.debug "Excluding $it in $tree.dir" }
    }
    return tree
  }

  private ConfigurableFileTree obtainFileTree(final String folder, final Iterable<String> excludes, final Iterable<String> includes) {
    return project.fileTree(folder) { ConfigurableFileTree tree ->
      tree.include includes
      tree.exclude excludes + DEFAULT_EXCLUDES
    }
  }
}
