package com.imaginea.serverlocator;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.serverlocator.model.KeyPairUserIdModel;
import com.imaginea.serverlocator.util.AWSConfigLoader;
import com.imaginea.serverlocator.util.AWSInstanceKeyPairLoader;
import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.Utils;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class RemoteSshProcessor implements Runnable, ApplicationConstants {

	private InstancesTalkingTopology parentTopologyPublisher;
	private JSONObject instanceJsonProperties;

	public RemoteSshProcessor(InstancesTalkingTopology parentTopologyPublisher,
			JSONObject instanceJsonProperties) {
		this.parentTopologyPublisher = parentTopologyPublisher;
		this.instanceJsonProperties = instanceJsonProperties;
	}

	@Override
	public void run() {
		JSch jsch = new JSch();
		try {
			KeyPairUserIdModel keyPairUserIdObj = AWSInstanceKeyPairLoader
					.getKeyPairUserIdModel(instanceJsonProperties
							.getString(TOPOLOGY_INSTANCE_NODE_INSTANCE_ID));
			jsch.addIdentity(Utils
					.getConfigPropertyAttribute("instanceKeyPairs.directory")
					+ keyPairUserIdObj.getKeyPair() + ".pem");
			Session session = jsch.getSession(keyPairUserIdObj.getUserId(),
					instanceJsonProperties
							.getString(TOPOLOGY_INSTANCE_NODE_PUBLIC_DNS),
					REMOTE_SSH_PORT_NO);
			UserInfo ui = new MyUserInfo();
			session.setUserInfo(ui);
			session.connect();

			String ipReqCmd = "/sbin/ifconfig | grep 'inet addr' | awk -F\":\" '{print $2}' | awk 'BEGIN {ORS = \";\"} {print $1}'";
			String localIpAddrSeq = getSshCmdOutput(session, ipReqCmd);
			String netStatReqCmd = "sudo netstat -tnap | awk 'BEGIN {OFS=\",\"; ORS= \";\"} NR > 2 {print $4,$5,$6,$7}'";
			String netstatOPSeq = getSshCmdOutput(session, netStatReqCmd);
			
			session.disconnect();
			parentTopologyPublisher.processNetStatRecs(netstatOPSeq,
					localIpAddrSeq);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getSshCmdOutput(Session session, String inputCmd)
			throws JSchException, IOException {
		StringBuilder commandOutput = new StringBuilder("");
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setPty(true);
		((ChannelExec) channel)
				.setCommand(inputCmd);
		channel.setInputStream(null);

		InputStream in = channel.getInputStream();
		channel.connect();

		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0)
					break;
				for (int k = 0; k < i; k++) {
					commandOutput.append((char) tmp[k]);
				}
			}
			if (channel.isClosed()) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (Exception ee) {
			}
		}
		channel.disconnect();
		return commandOutput.toString();
	}

	public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {
		public String getPassword() {
			return null;
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		String passphrase;
		JTextField passphraseField = (JTextField) new JPasswordField(20);

		public String getPassphrase() {
			return passphrase;
		}

		public boolean promptPassphrase(String message) {
			Object[] ob = { passphraseField };
			int result = JOptionPane.showConfirmDialog(null, ob, message,
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				passphrase = passphraseField.getText();
				return true;
			} else {
				return false;
			}
		}

		public boolean promptPassword(String message) {
			return true;
		}

		public void showMessage(String message) {
			JOptionPane.showMessageDialog(null, message);
		}

		final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0);
		private Container panel;

		public String[] promptKeyboardInteractive(String destination,
				String name, String instruction, String[] prompt, boolean[] echo) {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());

			gbc.weightx = 1.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.gridx = 0;
			panel.add(new JLabel(instruction), gbc);
			gbc.gridy++;

			gbc.gridwidth = GridBagConstraints.RELATIVE;

			JTextField[] texts = new JTextField[prompt.length];
			for (int i = 0; i < prompt.length; i++) {
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridx = 0;
				gbc.weightx = 1;
				panel.add(new JLabel(prompt[i]), gbc);

				gbc.gridx = 1;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weighty = 1;
				if (echo[i]) {
					texts[i] = new JTextField(20);
				} else {
					texts[i] = new JPasswordField(20);
				}
				panel.add(texts[i], gbc);
				gbc.gridy++;
			}

			if (JOptionPane.showConfirmDialog(null, panel, destination + ": "
					+ name, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
				String[] response = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					response[i] = texts[i].getText();
				}
				return response;
			} else {
				return null; // cancel
			}
		}
	}
}
