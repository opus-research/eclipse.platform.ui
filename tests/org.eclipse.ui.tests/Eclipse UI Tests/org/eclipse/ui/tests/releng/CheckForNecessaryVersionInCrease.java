package org.eclipse.ui.tests.releng;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class CheckForNecessaryVersionInCrease {

	@Test
	public void checkIfProjectNeedsVersionIncrease() {
		String url = "/home/vogella/git/eclipse.platform.ui/.git";

		File gitDir = new File(url);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IProject[] projects = root.getProjects();
		try {
			Repository repository = new FileRepository(gitDir);


			final ObjectId start = repository.resolve(Constants.HEAD);
			for (IProject project : projects) {
				RevWalk walk = new RevWalk(repository);
				IFile file = project.getFile("META-INF/MANIFEST.MF");


				if (!file.exists()) {
					// the project is not a plug-in
					// use the following line to see which project is reported
					// System.out.println(project.getName() + " has no MANIFEST.MF");
					continue;
				}

				List<String> lines = Files.readAllLines(file.getLocation().toFile().toPath(), StandardCharsets.UTF_8);
				String currentVersion = "";
				for (String string : lines) {
					if (string.startsWith("Bundle-Version: ")) {
						currentVersion = string.substring("Bundle-Version: ".length());
					}
				}
				final RepositoryMapping mapping = RepositoryMapping.getMapping(file);
				walk.setTreeFilter(AndTreeFilter.create(PathFilter.create(mapping.getRepoRelativePath(file)),
						TreeFilter.ANY_DIFF));
				walk.markStart(walk.lookupCommit(start));
				final RevCommit commit = walk.next();
				LocalDateTime ofEpochSecond = LocalDateTime.ofEpochSecond(Long.valueOf(commit.getCommitTime()), 0,
						ZoneOffset.ofHours(2));
				System.out.println(
						project.getName() + " last changed " + ofEpochSecond + " BundleVersion " + currentVersion);
				walk.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
