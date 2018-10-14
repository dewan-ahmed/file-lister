//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.testfixtures.ProjectBuilder

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.doReturn
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

@CompileStatic
class FileListerExtensionTest {

  private static final String TEST_FILE1 = 'testFile1.ext1'
  private static final String TEST_FILE2 = 'testFile2.ext2'
  private static final String TEST_DIR2 = 'dir2'
  private static final String GIT_IGNORE_FILE = '.gitignore'
  private static final Project project = ProjectBuilder.builder().build()
  private static final File testDir1 = new File(project.projectDir, 'fileListerTest')
  private static final File testFile1
  private static final File testFile2
  private static final File testDir2 = new File(project.projectDir, 'fileListerTest')
  private static final File testFile21
  private static final File testFile22

  private final File testGitIgnoreFile1 = new File(testDir1, GIT_IGNORE_FILE)
  private final File testGitIgnoreFile2 = new File(testDir2, GIT_IGNORE_FILE)

  static {
    testDir1.mkdir()
    testFile1 = new File(testDir1, TEST_FILE1)
    testFile1.createNewFile()
    testFile2 = new File(testDir1, TEST_FILE2)
    testFile2.createNewFile()
    testDir2 = new File(testDir1, TEST_DIR2)
    testDir2.mkdir()
    testFile21 = new File(testDir2, TEST_FILE1)
    testFile21.createNewFile()
    testFile22 = new File(testDir2, TEST_FILE2)
    testFile22.createNewFile()
    // Gradle folders & files
    new File(testDir1, 'gradlew.sh').createNewFile()
    final File testGradleDir1 = new File(testDir1, 'gradle')
    testGradleDir1.mkdir()
    new File(testGradleDir1, TEST_FILE1).createNewFile()
    final File testGradleDir2 = new File(testDir1, '.gradle')
    testGradleDir2.mkdir()
    new File(testGradleDir2, TEST_FILE1).createNewFile()
    final File testGradleBuildDir1 = new File(testDir1, 'build')
    testGradleBuildDir1.mkdir()
    new File(testGradleBuildDir1, TEST_FILE1).createNewFile()
    final File testGradleBuildDir2 = new File(testDir2, 'build')
    testGradleBuildDir2.mkdir()
    new File(testGradleBuildDir2, TEST_FILE1).createNewFile()
    final File testNodeModulesDir = new File(testDir2, 'node_modules')
    testNodeModulesDir.mkdir()
    new File(testNodeModulesDir, TEST_FILE1).createNewFile()
  }

  private void createGitIgnoreFiles() {
    testGitIgnoreFile1.createNewFile()
    testGitIgnoreFile2.createNewFile()
  }

  @AfterEach
  void afterTest() {
    testGitIgnoreFile1.delete()
    testGitIgnoreFile2.delete()
  }

  @Test
  void shouldObtainFullFileTree() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTree() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22]))
  }

  @Test
  void shouldObtainFullFileTreeWithPath() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPath() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithGitIgnoreFiles() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFiles() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithGitIgnoreFilesAndMatchingPattern() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1')
    testGitIgnoreFile2.write('/*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPattern() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1')
    testGitIgnoreFile2.write('/*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertFalse(fileTree.files.containsAll([testFile1, testFile22]))
 }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternEndingSlash() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('*.ext1/')
    testGitIgnoreFile2.write('*.ext2/')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertFalse(fileTree.files.containsAll([testFile1, testFile21, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndTwoSlashes() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1/')
    testGitIgnoreFile2.write('/*.ext2/')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertFalse(fileTree.files.containsAll([testFile1, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternNoSlash() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('*.ext1')
    testGitIgnoreFile2.write('*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertFalse(fileTree.files.containsAll([testFile1, testFile21, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternWithCommentsAndEmpties() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('# /*.ext1')
    testGitIgnoreFile1.write('   ')
    testGitIgnoreFile2.write('  # /*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternIgnorePattern() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1[sub]')
    testGitIgnoreFile2.write('*.ext2!end')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22]))
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPattern() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1')
    testGitIgnoreFile2.write('/*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile2, testFile21] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternEndingSlash() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('*.ext1/')
    testGitIgnoreFile2.write('*.ext2/')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile2] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndTwoSlashes() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1/')
    testGitIgnoreFile2.write('/*.ext2/')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile2, testFile21] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternNoSlash() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('*.ext1')
    testGitIgnoreFile2.write('*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile2] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternWithCommentsAndEmpties() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('# /*.ext1')
    testGitIgnoreFile1.write('   ')
    testGitIgnoreFile2.write('  # /*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternIgnorePattern() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1[sub]')
    testGitIgnoreFile2.write('*.ext2!end')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals([testFile1, testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithIncludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['*.ext1']])

    assertEquals([testFile1] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithIncludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['*.ext1']])

    assertEquals([testFile1] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWitheExcludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [excludes: ['*.ext1']])

    assertEquals([testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithExcludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['*.ext1']])

    assertEquals([testFile2, testFile21, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithIncludesAnyFolder() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['**/*.ext1']])

    assertEquals([testFile1, testFile21] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithIncludesAnyFolder() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['**/*.ext1']])

    assertEquals([testFile1, testFile21] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWitheExcludesAnyFolder() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [excludes: ['**/*.ext1']])

    assertEquals([testFile2, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithExcludesAnyFolder() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['**/*.ext1']])

    assertEquals([testFile2, testFile22] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternWithIncludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1')
    testGitIgnoreFile2.write('/*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['**/*.ext1']])

    assertEquals([testFile21] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternWithExcludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)
    createGitIgnoreFiles()
    testGitIgnoreFile1.write('/*.ext1')
    testGitIgnoreFile2.write('/*.ext2')

    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['**/*.ext1']])

    assertEquals([testFile2] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFileTreeWithIncludesAndExcludes() {
    final FileListerExtension fileLister = new FileListerExtension(project)

    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['*.ext1'], excludes: ['**/*.ext1']])

    assertEquals([] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFileTreeWithLogger() {
    final Project spyProject = spy(project)
    final Logger mockLogger = mock(Logger)
    final FileListerExtension fileLister = new FileListerExtension(spyProject)
    doReturn(mockLogger)
      .when(spyProject)
      .getLogger()
    doReturn(true)
      .when(mockLogger)
      .isDebugEnabled()

    fileLister.obtainFullFileTree(testDir1.path, [includes: ['*.ext1'], excludes: ['**/*.ext1']])

    verify(mockLogger, times(7)).debug(anyString())
  }
}
