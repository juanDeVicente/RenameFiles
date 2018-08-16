import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Font;

public class Window 
{
	private JFrame frmRenameFiles;
	private JTextField textField;
	private JFileChooser chooser;
	private String currentLocation;
	private JTree tree;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					Window window = new Window();
					window.frmRenameFiles.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() 
	{
		currentLocation = ClassLoader.getSystemClassLoader().getResource(".").getPath();
		createExtensionsFile();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frmRenameFiles = new JFrame();
		frmRenameFiles.setTitle("Rename files");
		frmRenameFiles.setSize(530, 309);
		frmRenameFiles.setLocationRelativeTo(null);
		frmRenameFiles.setResizable(false);
		frmRenameFiles.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRenameFiles.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(10, 209, 400, 20);
		frmRenameFiles.getContentPane().add(textField);
		textField.setColumns(10);
		textField.getDocument().addDocumentListener
		(
			new DocumentListener()
			{
				@Override
				public void changedUpdate(DocumentEvent arg0) 
				{
					action();
				}
				@Override
				public void insertUpdate(DocumentEvent arg0) 
				{
					action();
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) 
				{
					action();
				}
				private void action()
				{
					if(!exists() || (textField.getText() == null || textField.getText() == ""))
						tree.setModel(null);
					else if(isDirectory())
						createTreeModel();
				}
				private boolean exists()
				{
					File selectedDirectory = new File(textField.getText());
					return selectedDirectory.exists();
				}
				private boolean isDirectory()
				{
					File selectedDirectory = new File(textField.getText());
					return selectedDirectory.isDirectory();
				}
			}
		);
		
		JButton buttonBrowse = new JButton("Buscar");
		buttonBrowse.setFont(new Font("Tahoma", Font.PLAIN, 11));
		buttonBrowse.setBounds(420, 209, 79, 20);
		buttonBrowse.addActionListener
		(
			new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					chooser = new JFileChooser();
					if(textField.getText().equals("") || !(new File(textField.getText()).exists()))
						chooser.setCurrentDirectory(new File("."));
					else
						chooser.setCurrentDirectory(new File(textField.getText()));
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					if (chooser.showOpenDialog(frmRenameFiles) == JFileChooser.APPROVE_OPTION)
					{
						Window.this.textField.setText(chooser.getSelectedFile().getAbsolutePath());
						createTreeModel();
					}
				}
			}
		);
		frmRenameFiles.getContentPane().add(buttonBrowse);
		
		JButton buttonExit = new JButton("Salir");
		buttonExit.setFont(new Font("Tahoma", Font.PLAIN, 11));
		buttonExit.setBounds(330, 240, 79, 20);
		buttonExit.addActionListener
		(
			new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					System.exit(0);
				}
			}
		);
		frmRenameFiles.getContentPane().add(buttonExit);
		
		JLabel labelInfo = new JLabel("<html>Selecciona la carpeta donde est\u00E1n los archivos</html>");
		labelInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		labelInfo.setHorizontalAlignment(SwingConstants.CENTER);
		labelInfo.setBounds(10, 11, 400, 16);
		frmRenameFiles.getContentPane().add(labelInfo);
		
		JScrollPane extensionsScrollPanel = new JScrollPane();
		extensionsScrollPanel.setViewportBorder(UIManager.getBorder("Button.border"));
		extensionsScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		extensionsScrollPanel.setBounds(420, 30, 79, 134);
		frmRenameFiles.getContentPane().add(extensionsScrollPanel);
		
		JPanel extensionPanel = new JPanel();
		extensionPanel.setBorder(UIManager.getBorder("TextArea.border"));
		extensionsScrollPanel.setViewportView(extensionPanel);
		extensionPanel.setLayout(new BoxLayout(extensionPanel, BoxLayout.Y_AXIS));
		
		try (BufferedReader reader = new BufferedReader(new FileReader(currentLocation + File.separator + "Extensions")))
		{
			while(reader.ready())
			{
				JCheckBox checkBox = new JCheckBox("." + reader.readLine());
				extensionPanel.add(checkBox);
			}
		} 
		catch (FileNotFoundException e3) 
		{
			showError("Archivo \"Extensions\" no encontrado, se procederá a crear el archivo.");
			createExtensionsFile();
		}
		catch (IOException e3) 
		{
			showError("Error de entrada/salida");
			createDebugFile(e3);
		}
		
		JButton btnAddNewExtension = new JButton("A\u00F1adir");
		btnAddNewExtension.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnAddNewExtension.setBounds(420, 175, 79, 23);
		btnAddNewExtension.addActionListener
		(
			new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					String extension = JOptionPane.showInputDialog(frmRenameFiles, "Escribe la extensión que quieras añadir.", "EXTENSION", JOptionPane.QUESTION_MESSAGE);
					
					if(extension != null)
					{
						List<String> extensions = new LinkedList<String>();
						
						try (BufferedReader reader = new BufferedReader(new FileReader(currentLocation + "/Extensions")))
						{
							while(reader.ready())
								extensions.add(reader.readLine());
						} 
						catch (FileNotFoundException e2) 
						{
							showError("Archivo \"Extensions\" no encontrado, se procederá a crear el archivo.");
							createExtensionsFile();
						}
						catch (IOException e2) 
						{
							showError("Error de entrada/salida");
							e2.printStackTrace();
						}

						if( ((extension.startsWith(".") && noPoints(extension, 1)) || noPoints(extension, 0)) && !extensions.contains(extension.replaceFirst(".", "")))
						{
							extension = extension.replaceFirst(".", "");
							try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(currentLocation + "/Extensions", true))))
							{
								out.print('\n' + extension);
							    
								showSuccess("¡Extensión añadida con éxito!");
								extensionPanel.add(new JCheckBox("." + extension));
								Window.this.frmRenameFiles.validate();
								Window.this.frmRenameFiles.repaint();
							}
							catch (IOException e1) 
							{
								showError("Error de entrada/salida");
								createDebugFile(e1);
							}
						}
						else if(extensions.contains(extension.replaceFirst(".", "")))
						{
							showError("Extensión ya introducida");
							this.actionPerformed(e);
						}
						else
						{
							showError("Extensión introducida no válida");
							this.actionPerformed(e);
						}
					}
				}
			}
		);
		
		frmRenameFiles.getContentPane().add(btnAddNewExtension);
		
		JButton buttonChange = new JButton("Cambiar");
		buttonChange.setFont(new Font("Tahoma", Font.PLAIN, 11));
		buttonChange.setBounds(420, 240, 79, 20);
		buttonChange.addActionListener
		(
			new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					String path = textField.getText();
					File selectedDirectory = new File(path);
					if(!selectedDirectory.exists() || !selectedDirectory.isDirectory())
						showError("Directorio no válido");
					else
					{
						File[] allFiles = selectedDirectory.listFiles();
						Arrays.sort(allFiles);
						List<String> extensions = new LinkedList<String>();
						
						for(Component component : extensionPanel.getComponents())
						{
							JCheckBox box = (JCheckBox)component; 
							if(box.isSelected())
								extensions.add(box.getText().replace(".", ""));
						}
						
						int index = 0;
						for(int i = 0 ; i < allFiles.length ; i++)
						{
							
							if(allFiles[i].isFile())
							{
								int j = 0;
								for(j = 0 ; j < extensions.size() && !(allFiles[i].getName().endsWith(extensions.get(j)) || allFiles[i].getName().endsWith(extensions.get(j).toUpperCase())); j++);
								
								if(j < extensions.size())
									while(!allFiles[i].renameTo(new File(path + "/" + index++ + "." + extensions.get(j))))
										index++;
							}
						}
						showSuccess("Operación completada con éxito");
						System.exit(0);
					}
				}
			}
		);
		frmRenameFiles.getContentPane().add(buttonChange);
		
		JLabel lblExtensions = new JLabel("Extensiones");
		lblExtensions.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblExtensions.setHorizontalAlignment(SwingConstants.CENTER);
		lblExtensions.setBounds(420, 11, 79, 16);
		frmRenameFiles.getContentPane().add(lblExtensions);
		
		JScrollPane treeScrollPanel = new JScrollPane();
		treeScrollPanel.setBounds(10, 30, 399, 168);
		frmRenameFiles.getContentPane().add(treeScrollPanel);
		
		JPanel treePanel = new JPanel();
		treeScrollPanel.setViewportView(treePanel);
		treePanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		tree = new JTree();
		tree.setModel(null);
		tree.addMouseListener
		(
			new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					int selectedRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selectedPath = tree.getPathForLocation(e.getX(), e.getY());
					if(selectedRow != -1)
					{
						if(e.getClickCount() == 2 && ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK))
						{
							if(selectedPath.getPathCount() > 1)
							{
								Object[] objects = selectedPath.getPath();
								
								if(!textField.getText().endsWith(File.separator))
									textField.setText(textField.getText() + File.separator);
								
								String newPath = "";
								for(int i = 1 ; i < objects.length ; i++)
									newPath += objects[i] + File.separator;
								
								String path = textField.getText() + newPath;
								if(new File(path).isDirectory())
									textField.setText(path);
								
								treeScrollPanel.getViewport().setViewPosition(new Point(0, 0));
							}
							else if(selectedPath.getPathCount() == 1)
							{
								File f = new File(textField.getText());
								if(f.getParentFile() != null)
								{
									textField.setText(f.getParentFile().getAbsolutePath());
									createTreeModel();
								}
							}
						}
					}
				}
			}
		);
		tree.addTreeExpansionListener
		(
			new TreeExpansionListener()
			{
				@Override
				public void treeCollapsed(TreeExpansionEvent arg0) 
				{
					
				}

				@Override
				public void treeExpanded(TreeExpansionEvent arg0) 
				{
					
					String path = textField.getText();
					if(!path.endsWith(File.separator))
						path += File.separator;
					Object[] objects = arg0.getPath().getPath();
					
					for(int i = 1 ; i < objects.length ; i++)
						path += objects[i].toString() + File.separator;
					
					File file = new File(path);
					File[] childFiles = file.listFiles();
					
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)arg0.getPath().getLastPathComponent();
					node.removeAllChildren();
					
					if(childFiles != null && childFiles.length > 0)
					{
						for(File f : childFiles)
						{
							DefaultMutableTreeNode aux = new DefaultMutableTreeNode(f.getName()); 
							if(f.isDirectory())
								aux.add(new DefaultMutableTreeNode("dummy"));
							node.add(aux);
						}
					}
					else
						node.add(new DefaultMutableTreeNode("¡El directorio está vacío!"));
					
					((DefaultTreeModel)tree.getModel()).nodeStructureChanged(node);
				}
				
			}
		);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treePanel.add(tree);
	}
	private boolean noPoints(String extension, int pos)
	{
		int i = 0;
		for(i = pos ; i < extension.length() && extension.charAt(i) != '.' ; i++);
		
		return i > 1 && i == extension.length();
	}
	private void showSuccess(String message)
	{
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(frmRenameFiles, message, "EXITO", JOptionPane.INFORMATION_MESSAGE);
	}
	private void showError(String message)
	{
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(frmRenameFiles, message, "ERROR", JOptionPane.ERROR_MESSAGE);
	}
	private void createExtensionsFile()
	{
		File f = new File(currentLocation + File.separator + "Extensions"); 
		if(!f.exists())
		{
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(currentLocation + File.separator + "Extensions", true))))
			{
				f.createNewFile();
				out.println("jpg");
				out.println("png");
				out.println("tff");
				out.print("svg");
			}
			catch (IOException e) 
			{
				showError("Error de entrada/salida.");
				createDebugFile(e);
			}
		}
	}
	private void createDebugFile(Exception e)
	{
		try(PrintWriter pw = new PrintWriter("Debug.txt"))
		{
			e.printStackTrace(pw);
		}
		catch (FileNotFoundException e1) 
		{
			showError("Imposible crear el archivo de debug");
		}
	}
	private void createTreeModel()
	{
		File f = new File(textField.getText());
		tree.setModel
		(
			new DefaultTreeModel
			(
				new DefaultMutableTreeNode(f.getName().equals("")? f.getAbsolutePath() : f.getName()) 
				{
					private static final long serialVersionUID = 1L;
					{
						DefaultMutableTreeNode node_1;
						File[] files = f.listFiles();
						
						if(files != null)
						{
							for(File file : files)
							{
								node_1 = new DefaultMutableTreeNode(file.getName());
								if(file.isDirectory())
									node_1.add(new DefaultMutableTreeNode("dummy"));
								add(node_1);
							}
						}
					}
				}
			)
		);
	}
}