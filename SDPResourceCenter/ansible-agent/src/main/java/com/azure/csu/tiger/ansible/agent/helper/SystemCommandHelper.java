package com.azure.csu.tiger.ansible.agent.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SystemCommandHelper {
	
	private final static Logger logger = LoggerFactory.getLogger(SystemCommandHelper.class);
	
	@Value("${ansible.cfg.path}")
    private String ansibleCfgPath;

	@Value("${ansible.cfg.name}")
    private String ansibleCfgName;

	@Value("${ansible.workdir}")
    private String ansibleWorkdir;
	
	boolean stopped = false;

	
	public List<String> executeCommand(List<String> command, int timeout) throws IOException {
		Process process = null;
		Thread stdoutThread = null;
		Thread stderrThread = null;
		List<String> stdout = new ArrayList<String>();
		List<String> stderr = new ArrayList<String>();
		try {
			logger.info("execute system command: "+command.stream().collect(Collectors.joining(" ")));
			ProcessBuilder pb = new ProcessBuilder(command);
			//ProcessBuilder pb = new ProcessBuilder( "/usr/bin/ansible-playbook", "-i", "/home/sdp/ansible/inventory/hosts", "/home/sdp/ansible/plbook/playbook-unittest-success.yaml", "-u", "azureuser", "--key-file", "/home/sdp/ansible/privatekey/vm1_key.pem" ); 
            pb.directory(new File(ansibleWorkdir)); 
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            pb.redirectErrorStream(true);

            Map<String,String> env = pb.environment();

            env.put("ANSIBLE_CONFIG", ansibleCfgPath+ansibleCfgName);

            process = pb.start();

			final Process finalProcess = process;
			// 读取进程的标准输出流的线程
			stdoutThread = new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						stdout.add(line);  // 处理输出
					}
				} catch (IOException e) {
					logger.error("Error reading stdout", e);
				}
			});

			// 读取进程的标准错误流的线程
			stderrThread = new Thread(() -> {
				try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(finalProcess.getErrorStream()))) {
					String line;
					while ((line = errorReader.readLine()) != null) {
						stderr.add(line);  // 处理错误输出
					}
				} catch (IOException e) {
					logger.error("Error reading stderr", e);
				}
			});

			// 启动读取流的线程
			stdoutThread.start();
			stderrThread.start();

			boolean completed  = process.waitFor(15, TimeUnit.MINUTES);
			logger.info("\nExited. Completed: {}", completed);

			if (!completed) {
				logger.info("Job Failed. Timeout reached after 15 minutes.");
				stdout.add(0, "Job Failed.\n");
			} else {
				// 等待流的线程完成
				stdoutThread.join();
				stderrThread.join();
			}

		}catch(Exception e) {
			logger.error("error execute system command", e);
			throw new RemoteException(e.getMessage());
		}
		finally {
			if (stdoutThread != null) {
				stdoutThread.interrupt();
			}
			if (stderrThread != null) {
				stderrThread.interrupt();
			}
			if(process!=null) {
				try {
					if (process.getInputStream() != null) {
						process.getInputStream().close();
					}
					if (process.getErrorStream() != null) {
						process.getErrorStream().close();
					}
					process.destroyForcibly();
				}catch(Exception e) {
					logger.error("Ansible ProcessBuilder.Destroy forcibly", e);
					throw new RuntimeException(e);
				}
			}
		}
		stdout.addAll(stderr);
		logger.info("execute system command info:\n {}", stdout.stream().collect(Collectors.joining("\n")));
		return stdout;
	}
}
