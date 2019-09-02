//  Copyright (c) 2018 Gonzalo Müller Bravo.
//  Licensed under the MIT License (MIT), see LICENSE.txt
package all.shared.gradle.file

import all.shared.gradle.testfixtures.SpyProjectFactory

import groovy.transform.CompileStatic

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@CompileStatic
class FileListerExtensionTest {

  private static final String TEST_FILE1 = 'testFile1.ext1'
  private static final String TEST_FILE2 = 'testFile2.ext2'
  private static final String TEST_DIR2 = 'dir2'
  private static final String GIT_IGNORE_FILE = '.gitignore'

  private static final Project spyProject = SpyProjectFactory.build()

  private static final File testDir1 = new File(spyProject.projectDir, 'fileListerTest')
  private static final File testFile1
  private static final File testFile2
  private static final File testDir2 = new File(spyProject.projectDir, 'fileListerTest')
  private static final File testFile21
  private static final File testFile22

  private static final List<File> moreTestFiles
  private static final List<File> moreTestFiles1
  private static final List<File> moreTestFiles2
  private static final List<File> moreTestFiles3

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
    final File testGradleDir1 = new File(testDir1, 'gradle')
    testGradleDir1.mkdir()
    final File testGradleDir2 = new File(testDir1, '.gradle')
    testGradleDir2.mkdir()
    new File(testGradleDir2, TEST_FILE1)
      .createNewFile()
    final File testGradleBuildDir1 = new File(testDir1, 'build')
    testGradleBuildDir1.mkdir()
    final File testGradleBuildDir2 = new File(testDir2, 'build')
    testGradleBuildDir2.mkdir()
    final File testNodeModulesDir = new File(testDir2, 'node_modules')
    testNodeModulesDir.mkdir()
    moreTestFiles1 = [
      new File(testGradleDir1, TEST_FILE1),
      new File(testGradleBuildDir1, TEST_FILE1),
    ]
    moreTestFiles2 = [
      new File(testDir1, 'gradlew.sh'),
    ]
    moreTestFiles3 = [
      new File(testGradleBuildDir2, TEST_FILE1),
      new File(testNodeModulesDir, TEST_FILE1)
    ]
    moreTestFiles = moreTestFiles1 + moreTestFiles2 + moreTestFiles3
    moreTestFiles.each { it.createNewFile() }
  }

  private final FileListerExtension fileLister = new FileListerExtension(spyProject)

  @Test
  void shouldObtainFullFileTree() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22] + moreTestFiles))
  }

  @Test
  void shouldObtainPartialFileTree() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree()

    assertTrue(fileTree.files.containsAll([testFile1, testFile2, testFile21, testFile22] + moreTestFiles))
  }

  @Test
  void shouldObtainFullFileTreeWithPath() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

    assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithPath() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

    assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
  }

  @Nested
  public class WithMoreFileTest {
    private final File testGitIgnoreFile1 = new File(testDir1, GIT_IGNORE_FILE)
    private final File testGitIgnoreFile2 = new File(testDir2, GIT_IGNORE_FILE)

    @BeforeEach
    void beforeTest() {
      testGitIgnoreFile1.createNewFile()
      testGitIgnoreFile2.createNewFile()
      testGitIgnoreFile1.write('gradle/\nnode_modules/\nbuild/\n')
    }

    @AfterEach
    void afterTest() {
      testGitIgnoreFile1.delete()
      testGitIgnoreFile2.delete()
    }

    @Test
    void shouldObtainFullFileTreeWithGitIgnoreFiles() {
      final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

      assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithGitIgnoreFiles() {
      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithInternalGitIgnoreFiles() {
      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir2.path)

      assertEquals(([testFile21, testFile22] + moreTestFiles3) as Set, fileTree.files)
    }

    @Test
    void shouldObtainFullFileTreeWithGitIgnoreFilesAndMatchingPattern() {
      testGitIgnoreFile1.append('/*.ext1')
      testGitIgnoreFile2.write('/*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path)

      assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPattern() {
      testGitIgnoreFile1.append('/*.ext1')
      testGitIgnoreFile2.write('/*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile2, testFile21] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternEndingSlash() {
      testGitIgnoreFile1.append('*.ext1/')
      testGitIgnoreFile2.write('*.ext2/')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile2] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndTwoSlashes() {
      testGitIgnoreFile1.append('/*.ext1/')
      testGitIgnoreFile2.write('/*.ext2/')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile2, testFile21] + moreTestFiles2)  as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternNoSlash() {
      testGitIgnoreFile1.append('*.ext1')
      testGitIgnoreFile2.write('*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile2] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternWithCommentsAndEmpties() {
      testGitIgnoreFile1.append('# /*.ext1\n')
      testGitIgnoreFile1.append('   ')
      testGitIgnoreFile2.write('  # /*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithPathAndGitIgnoreFilesAndMatchingPatternIgnorePattern() {
      testGitIgnoreFile1.append('/*.ext1[sub]')
      testGitIgnoreFile2.write('*.ext2!end')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path)

      assertEquals(([testFile1, testFile2, testFile21, testFile22] + moreTestFiles2) as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternWithIncludes() {
      testGitIgnoreFile1.append('/*.ext1')
      testGitIgnoreFile2.write('/*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['**/*.ext1']])

      assertEquals([testFile21] as Set, fileTree.files)
    }

    @Test
    void shouldObtainPartialFileTreeWithGitIgnoreFilesAndMatchingPatternWithExcludes() {
      testGitIgnoreFile1.append('/*.ext1')
      testGitIgnoreFile2.write('/*.ext2')

      final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['**/*.ext1']])

      assertEquals(([testFile2] + moreTestFiles2) as Set, fileTree.files)
    }
  }

  @Test
  void shouldObtainFullFileTreeWithIncludes() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['*.ext1']])

    assertEquals([testFile1] as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithIncludes() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['*.ext1']])

    assertEquals([testFile1] as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithExcludes() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [excludes: ['*.ext1']])

    assertEquals(([testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithExcludes() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['*.ext1']])

    assertEquals(([testFile2, testFile21, testFile22] + moreTestFiles) as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithIncludesAnyFolder() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['**/*.ext1']])

    assertEquals(([testFile1, testFile21] + moreTestFiles1 + moreTestFiles3) as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithIncludesAnyFolder() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [includes: ['**/*.ext1']])

    assertEquals(([testFile1, testFile21] + moreTestFiles1 + moreTestFiles3) as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithExcludesAnyFolder() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [excludes: ['**/*.ext1']])

    assertEquals(([testFile2, testFile22] + moreTestFiles2) as Set, fileTree.files)
  }

  @Test
  void shouldObtainPartialFileTreeWithExcludesAnyFolder() {
    final ConfigurableFileTree fileTree = fileLister.obtainPartialFileTree(testDir1.path, [excludes: ['**/*.ext1']])

    assertEquals(([testFile2, testFile22] + moreTestFiles2) as Set, fileTree.files)
  }

  @Test
  void shouldObtainFullFileTreeWithIncludesAndExcludes() {
    final ConfigurableFileTree fileTree = fileLister.obtainFullFileTree(testDir1.path, [includes: ['*.ext1'], excludes: ['**/*.ext1']])

    assertEquals([] as Set, fileTree.files)
  }
}
