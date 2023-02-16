package com.juse.emulator.util.process;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import com.juse.emulator.util.config.ResourceCopy;

public class ProcessUtil
{
	public static void relauchWithExt(Class whichClass,String extDir, List<String> args) 
	{		
		final List<String> cpe = new ArrayList<String>();		
		try
		{
			for(int i=0;i<args.size();i++)
			{
				String an = args.get(i);
				if(an.equals("--ext"))
				{
					args.set(i++,"");
					args.set(i,"");
				}
			}
			
			Path extPath = Paths.get(extDir);
			Files.walkFileTree(extPath, new FileVisitor<Path>() 
			{

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
				{
					//System.out.println("FOUND DIR:" + dir.toAbsolutePath());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					String catFile = file.toFile().getCanonicalPath();
					
					if(catFile.toLowerCase().endsWith(".jar"))
					{
						System.out.println("Found Extension Jar:" + file.toFile().getCanonicalPath());
						cpe.add(file.toFile().getCanonicalPath());
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
				{
					//System.out.println("FAILED:" + file.toAbsolutePath());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					//System.out.println("FOUND:" + dir.toAbsolutePath());
					return FileVisitResult.CONTINUE;
				}

			});
			
			List<String> jvmArgs = new ArrayList<String>();
			int code = exec(whichClass,cpe,jvmArgs,args);
			System.exit(code);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int exec(Class clazz, List<String> extClassPath, List<String> jvmArgs, List<String> args) throws IOException, InterruptedException
	{
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = clazz.getName();
		
		if(extClassPath!=null && extClassPath.size() > 0)
		{
			for(String cpe : extClassPath)
			{
				classpath+=(File.pathSeparator  + cpe);
			}
		}
		
		List<String> command = new ArrayList<>();
		command.add(javaBin);
		command.addAll(jvmArgs);
		command.add("-cp");
		command.add(classpath);
		command.add(className);
		command.addAll(args);

		for(String c : command)
		{
			System.out.println("EXT ARG:" + c);
		}
		
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.inheritIO().start();
		process.waitFor();
		return process.exitValue();
	}

	public static void extractResources(Class loadingClass, String resName,String destination)
	{
		URL fromURL = loadingClass.getClassLoader().getResource(resName);
		File toPath = new File(destination);

		if (!toPath.exists())
		{
			String url = fromURL.toString();
			String jarName = url.substring(0, url.indexOf('!')).replace("jar:file:", "");

			try
			{
				// System.out.println("fromURL:" + fromURL);
				// System.out.println("toPath:" + toPath.getAbsolutePath());
				System.out.println("Copying demo directory:\nFrom: " + jarName + "\nTo  : " + toPath.getCanonicalPath());

				ResourceCopy rc = new ResourceCopy();
				rc.copyResourceDirectory(new JarFile(jarName), resName, toPath);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("The demo directory already exists.");
		}
		// System.exit(0);
	}

}
