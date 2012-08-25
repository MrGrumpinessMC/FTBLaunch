package net.ftb.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.ftb.data.LoginResponse;
import net.ftb.data.Settings;
import net.ftb.workers.GameUpdateWorker;
import net.ftb.workers.LoginWorker;

import org.eclipse.wb.swing.FocusTraversalOnArray;

public class LauncherFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param args
	 *            Program arguments.
	 */
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Load settings
		try
		{
			Settings.initSettings();
		} catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to load config file: "
					+ e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		// Create the install directory if it does not exist.
		File installDir = new File(Settings.getSettings().getInstallPath());
		if (!installDir.exists())
			installDir.mkdirs();
		
		LauncherFrame mainFrame = new LauncherFrame();
		mainFrame.setVisible(true);
	}
	
	public LauncherFrame()
	{
		super("FTB Launcher");
		
		initGui();
	}
	
	public void initGui()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		final int width = 1000;
		final int height = 600;
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int) size.getWidth() / 2 - width / 2, (int) size.getHeight()
				/ 2 - height / 2, width, height);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		newsPane = new JTextPane();
		newsPane.setContentType("text/html");
		newsPane.setText("<html><body>insert news here</body></html>");
		newsPane.setEditable(false);
		
		JScrollPane newsScroll = new JScrollPane(newsPane);
		getContentPane().add(newsScroll, BorderLayout.CENTER);
		
		bottomPanel = new JPanel();
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		horizontalFiller = Box.createHorizontalGlue();
		bottomPanel.add(horizontalFiller, BorderLayout.CENTER);
		
		// Login panel
		loginPanel = new JPanel();
		bottomPanel.add(loginPanel, BorderLayout.EAST);
		GridBagLayout gbl_loginPanel = new GridBagLayout();
		gbl_loginPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_loginPanel.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gbl_loginPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		loginPanel.setLayout(gbl_loginPanel);
		
		lblError = new JLabel();
		lblError.setForeground(Color.RED);
		lblError.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.gridwidth = 3;
		gbc_lblError.insets = new Insets(4, 0, 5, 0);
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 0;
		loginPanel.add(lblError, gbc_lblError);
		
		lblUsername = new JLabel("Username:");
		lblUsername.setDisplayedMnemonic('u');
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.anchor = GridBagConstraints.EAST;
		gbc_lblUsername.gridx = 0;
		gbc_lblUsername.gridy = 1;
		loginPanel.add(lblUsername, gbc_lblUsername);
		
		btnOptions = new JButton("Options");
		btnOptions.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				OptionsDialog optionsDlg = new OptionsDialog();
				optionsDlg.setVisible(true);
			}
		});
		GridBagConstraints gbc_btnOptions = new GridBagConstraints();
		gbc_btnOptions.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOptions.insets = new Insets(0, 0, 5, 8);
		gbc_btnOptions.gridx = 2;
		gbc_btnOptions.gridy = 1;
		loginPanel.add(btnOptions, gbc_btnOptions);
		
		lblPassword = new JLabel("Password:");
		lblPassword.setDisplayedMnemonic('p');
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.insets = new Insets(0, 8, 5, 5);
		gbc_lblPassword.anchor = GridBagConstraints.EAST;
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 2;
		loginPanel.add(lblPassword, gbc_lblPassword);
		
		usernameField = new JTextField("", 17);
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.insets = new Insets(0, 0, 5, 5);
		gbc_usernameField.gridx = 1;
		gbc_usernameField.gridy = 1;
		loginPanel.add(usernameField, gbc_usernameField);
		
		passwordField = new JPasswordField("", 17);
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 2;
		loginPanel.add(passwordField, gbc_passwordField);
		
		chckbxRemember = new JCheckBox("Remember Password");
		GridBagConstraints gbc_chckbxRemember = new GridBagConstraints();
		gbc_chckbxRemember.insets = new Insets(0, 0, 4, 5);
		gbc_chckbxRemember.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxRemember.gridx = 1;
		gbc_chckbxRemember.gridy = 3;
		loginPanel.add(chckbxRemember, gbc_chckbxRemember);
		
		btnLogin = new JButton("Login");
		btnLogin.addActionListener(this);
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLogin.insets = new Insets(0, 0, 5, 8);
		gbc_btnLogin.gridx = 2;
		gbc_btnLogin.gridy = 2;
		loginPanel.add(btnLogin, gbc_btnLogin);
		
		verticalStrut = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridy = 1;
		gbc_verticalStrut.gridx = 0;
		loginPanel.add(verticalStrut, gbc_verticalStrut);
		
		setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] {
				usernameField, passwordField, chckbxRemember, btnLogin,
				btnOptions, newsScroll }));
	}
	
	public void doLogin()
	{
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);
		
		lblError.setForeground(Color.black);
		lblError.setText("Logging in...");
		
		LoginWorker loginWorker = new LoginWorker(usernameField.getText(),
				new String(passwordField.getPassword()))
		{
			@Override
			public void done()
			{
				lblError.setText("");
				
				btnLogin.setEnabled(true);
				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);
				
				String responseStr;
				try
				{
					responseStr = get();
				} catch (InterruptedException err)
				{
					err.printStackTrace();
					return;
				} catch (ExecutionException err)
				{
					err.printStackTrace();
					if (err.getCause() instanceof IOException)
					{
						lblError.setForeground(Color.red);
						lblError.setText("Login failed: "
								+ err.getCause().getMessage());
					}
					else if (err.getCause() instanceof MalformedURLException)
					{
						lblError.setForeground(Color.red);
						lblError.setText("Error: Malformed URL");
					}
					return;
				}
				
				LoginResponse response;
				try
				{
					response = new LoginResponse(responseStr);
				} catch (IllegalArgumentException e)
				{
					lblError.setForeground(Color.red);
					
					if (responseStr.contains(":"))
					{
						lblError.setText("Received invalid response from server.");
					}
					else
					{
						if (responseStr.equalsIgnoreCase("bad login"))
							lblError.setText("Invalid username or password.");
						else if (responseStr.equalsIgnoreCase("old version"))
							lblError.setText("Outdated launcher.");
						else
							lblError.setText("Login failed: " + responseStr);
					}
					return;
				}
				
				lblError.setText("Login complete.");
				runGameUpdater(response);
			}
		};
		loginWorker.execute();
	}
	
	public void runGameUpdater(LoginResponse response)
	{
		btnLogin.setEnabled(false);
		btnOptions.setEnabled(false);
		usernameField.setEnabled(false);
		passwordField.setEnabled(false);
		chckbxRemember.setEnabled(false);
		
		final ProgressMonitor progMonitor = 
				new ProgressMonitor(this, "Downloading minecraft...", "", 0, 100);
		
		final GameUpdateWorker updater = new GameUpdateWorker(response.getLatestVersion(), 
				"minecraft.jar", 
				new File(Settings.getSettings().getInstallPath(), "bin").getPath(), 
				false)
		{
			public void done()
			{
				btnLogin.setEnabled(true);
				btnOptions.setEnabled(true);
				usernameField.setEnabled(true);
				passwordField.setEnabled(true);
				chckbxRemember.setEnabled(true);
				
				progMonitor.close();
				try
				{
					if (get() == true)
					{
						// Success
						lblError.setForeground(Color.black);
						lblError.setText("Game update complete.");
					}
					else
					{
						lblError.setForeground(Color.red);
						lblError.setText("Error downloading game.");
					}
				} catch (CancellationException e)
				{
					lblError.setForeground(Color.black);
					lblError.setText("Game update cancelled...");
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				} catch (ExecutionException e)
				{
					e.printStackTrace();
					lblError.setForeground(Color.red);
					lblError.setText("Failed to download game: " + e.getCause().getMessage());
					return;
				}
			}
		};
		
		updater.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (progMonitor.isCanceled())
				{
					updater.cancel(false);
				}
				
				if (!updater.isDone())
				{
					int prog = updater.getProgress();
					if (prog < 0)
						prog = 0;
					else if (prog > 100)
						prog = 100;
					progMonitor.setProgress(prog);
					progMonitor.setNote(updater.getStatus());
				}
			}
		});
		updater.execute();
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equalsIgnoreCase("login"))
		{
			doLogin();
		}
	}
	
	private JPanel bottomPanel;
	private JPanel loginPanel;
	
	private JTextPane newsPane;
	
	private JTextField usernameField;
	private JPasswordField passwordField;
	private Component horizontalFiller;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JCheckBox chckbxRemember;
	private JButton btnLogin;
	private JButton btnOptions;
	private Component verticalStrut;
	private JLabel lblError;
}
