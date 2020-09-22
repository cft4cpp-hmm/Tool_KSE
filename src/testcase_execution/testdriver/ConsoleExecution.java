package testcase_execution.testdriver;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.ibm.icu.util.Calendar;
import config.Paths;
import utils.Utils;

/**
 * Execute in console
 *
 * @author DucAnh
 */
public class ConsoleExecution {

	/**
	 * Compile make file
	 *
	 * @param makefilePath
	 * @throws Exception
	 */
	public static void compileMakefile(File makefilePath) throws Exception {
		Date startTime = Calendar.getInstance().getTime();
		try {
			String command = "g++ -c "+ Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH +"/praticalTest.cpp -o praticalTest.o";

			Process p = Runtime.getRuntime().exec(command, null, new File(Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH));
			p.waitFor();
			
			String command2 ="g++ "+ Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH+"/praticalTest.o -o "+Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH+"/program.exe";

			Process p2 = Runtime.getRuntime().exec(command2, null, new File(Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH));
			p2.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Date end = Calendar.getInstance().getTime();
		}
	}

	/**
	 * Execute .exe
	 *
	 * @param exePath
	 */
	public static boolean executeExe(File exePath) throws Exception {
		boolean isTerminated = false;
		String command = "";
		if (Utils.isWindows())
			command = "\"" + exePath.getCanonicalPath() + "\"";

		else if (Utils.isUnix()) {
			command = exePath.getCanonicalPath();
		} else if (Utils.isMac()) {
			command = exePath.getCanonicalPath();
		}

		Date startTime = Calendar.getInstance().getTime();
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor(30, TimeUnit.SECONDS);

		if (p.isAlive()) {
			p.destroy(); // tell the process to stop
			p.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
			p.destroyForcibly(); // tell the OS to kill the process
			p.waitFor();
			isTerminated = true;
		}
		Date end = Calendar.getInstance().getTime();
		//AbstractAutomatedTestdataGeneration.executionTime += end.getTime() - startTime.getTime();
		return isTerminated;
	}

	/**
	 * Kill a process
	 *
	 * @param processName
	 * @throws Exception
	 */
	public static void killProcess(String processName) throws Exception {
		try {
			Runtime.getRuntime().exec("taskkill /F /IM " + processName);
		} catch (Exception e) {
			throw new Exception("Cannot kill process " + processName);
		}
	}
}
