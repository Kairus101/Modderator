package org.Kairus.Modderator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.Kairus.Modderator.onlineModDescription;
import org.Kairus.Modules.GameModule;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;
	Object[][] tableData;
	Object[][] table2Data;
	JMenuBar GUImenuBar = new JMenuBar();
	JMenu GUImenu = new JMenu("File");
	JMenu GUIsettings = new JMenu("Settings");
	JMenu GUIhelp = new JMenu("Help");
	JMenuItem GUIcreateBat = new JMenuItem("Create game launcher");
	JMenuItem GUIhelpUser = new JMenuItem("User Help");
	JMenuItem GUIhelpDev = new JMenuItem("Developer Help");
	JMenuItem GUIhelpAbout = new JMenuItem("About");
	JMenuItem GUIhelpChangeLog = new JMenuItem("Change Log");
	JMenuItem GUIexit = new JMenuItem("Exit");
	JLabel GUImodName = new JLabel("");
	JLabel GUImodAuthor = new JLabel("");
	JLabel GUImodVersion = new JLabel("");
	JButton GUIdownloadMod = new JButton("Download mod");
	JButton GUIapplyMods = new JButton("Apply mods");
	JCheckBoxMenuItem GUIdevMode = new JCheckBoxMenuItem("developer mode");
	ModsTableModel table;
	OnlineModsTableModel table2;
	ImageIcon defaultIcon = new ImageIcon("absolutely nothing.png");

	JComponent panel1 = new JPanel();
	JComponent panel2 = new JPanel();
	JTabbedPane tabbedPane = new JTabbedPane();
	

	JComponent gameSelectPanel = new JPanel();

	JTextField filterMods = new JTextField(10);
	JTextField filterOnline = new JTextField(10);

	Modderator modderator;

	TableRowSorter<DefaultTableModel> sorter;
	TableRowSorter<DefaultTableModel> sorter2;
	private void newFilter() {
		RowFilter<DefaultTableModel, Object> rf = null;
		try {
			ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object,Object>>(2);
			rfs.add(RowFilter.regexFilter(Pattern.compile("(?i)"+filterMods.getText()).toString(),2,3,4,5));
			rf = RowFilter.orFilter(rfs);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sorter.setRowFilter(rf);
	}
	private void newFilter2() {
		RowFilter<DefaultTableModel, Object> rf = null;
		try {
			ArrayList<RowFilter<Object, Object>> rfs = new ArrayList<RowFilter<Object,Object>>(2);
			rfs.add(RowFilter.regexFilter(Pattern.compile("(?i)"+filterOnline.getText()).toString(),0,2,3,4));
			rf = RowFilter.orFilter(rfs);
		} catch (java.util.regex.PatternSyntaxException e) {
			return;
		}
		sorter2.setRowFilter(rf);
	}

	GUI(Modderator mm){
		// setup
		super("Modderator");
		modderator = mm;
	}
	

	public void showMainScreen(){
		
		if ( modderator.mods.size()>0 && showYesNo("Update mods?", "Would you like to update your mods?") == 0){ //0 is yes.
			//update mods
			int updated = modderator.checkForModUpdates();
			if (updated>0){
				showMessage("Updated:\n"+updated);
			}else if(updated == 0)
				showMessage("All mods up to date!");
		}
		
		// layout
		setLayout(new BorderLayout());
		panel1.setLayout(new BorderLayout());
		panel2.setLayout(new BorderLayout());

		// menu
		GUImenu.add(GUIcreateBat);
		GUImenu.add(GUIexit);
		GUImenuBar.add(GUImenu);

		GUIdevMode.setSelected(modderator.isDeveloper);
		GUIsettings.add(GUIdevMode);
		GUImenuBar.add(GUIsettings);
		
		GUIhelp.add(GUIhelpUser);
		GUIhelp.add(GUIhelpDev);
		GUIhelp.add(GUIhelpAbout);
		GUIhelp.add(GUIhelpChangeLog);
		GUImenuBar.add(GUIhelp);

		add(GUImenuBar, BorderLayout.NORTH);


		String[] columnNames = {"Enabled", "Icon", "Name", "Author", "Version", "Category"};

		DefaultTableModel dataModel = new DefaultTableModel(tableData, columnNames);
		table = new ModsTableModel(dataModel);
		sorter = new TableRowSorter<DefaultTableModel>((DefaultTableModel) table.getModel());
		table.setRowSorter(sorter);
		//table.set
		table.setRowHeight(50);
		table.setPreferredScrollableViewportSize(new Dimension(200, 70));
		table.setFillsViewportHeight(true);
		
		table.getColumnModel().getColumn(2).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMaxWidth(55);
		sorter.setSortable(0, false);
		sorter.setSortable(1, false);
		sorter.setSortable(2, false);
		sorter.setSortable(3, false);
		sorter.setSortable(4, false);
		table.getTableHeader().setReorderingAllowed(false);
		table.removeColumn(table.getColumnModel().getColumn(5));
		JScrollPane modPanel = new JScrollPane(table);
		panel1.add(modPanel, BorderLayout.CENTER);

		//information panel
		JPanel infoPanel = new JPanel();
		infoPanel.add(new JLabel("Mod name: "));
		infoPanel.add(GUImodName);
		infoPanel.add(new JLabel("Mod author: "));
		infoPanel.add(GUImodAuthor);
		infoPanel.add(new JLabel("Mod version: "));
		infoPanel.add(GUImodVersion);
		infoPanel.setPreferredSize(new Dimension(150,0));
		panel1.add(new JScrollPane(infoPanel), BorderLayout.EAST);


		//bottom bar
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("Filter name/author/category"));
		bottomPanel.add(filterMods);
		filterMods.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				newFilter();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				newFilter();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				newFilter();
			}});
		bottomPanel.add(GUIapplyMods);
		bottomPanel.setPreferredSize(new Dimension(0,50));
		panel1.add(bottomPanel, BorderLayout.SOUTH);

		//bottom bar
		bottomPanel = new JPanel();
		GUIdownloadMod.setEnabled(false);
		bottomPanel.add(new JLabel("Filter name/author/description/category"));
		bottomPanel.add(filterOnline);
		filterOnline.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				newFilter2();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				newFilter2();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				newFilter2();
			}});
		bottomPanel.add(GUIdownloadMod);
		bottomPanel.setPreferredSize(new Dimension(0,50));
		panel2.add(bottomPanel, BorderLayout.SOUTH);


		// events
		GUIdownloadMod.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (showYesNo("Are you sure you want to download mod", "Are you sure you want to download:\n\n"+modDownloadName) == 0){
					//download time!
					modderator.downloadMod(modderator.repoPath+modDownloadLink.replace(" ", "%20"), "mods/"+modDownloadLink.substring(modDownloadLink.lastIndexOf("/")+1), modDownloadName);
					int o = 0;
					for (onlineModDescription i:modderator.onlineModList){
						if (i.link.equals(modDownloadLink)){
							removeFromTable2(o);
							modderator.onlineModList.remove(o);
							makeTable2Data();
							break;
						}
						o++;
					}
				}
			}});
		GUIexit.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}});
		GUIcreateBat.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int kbChunks = 1 << 10; //1kb
					java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(modderator.gameModule.exeURL).openStream());
					java.io.FileOutputStream fos = new java.io.FileOutputStream("Modded "+modderator.gameModule.name+".exe");
					java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,kbChunks*1024);
					byte[] data = new byte[kbChunks*1024];
					int x=0;
					while((x=in.read(data,0,kbChunks*1024))>=0)
						bout.write(data,0,x);
					bout.flush();
					bout.close();
					in.close();
					showMessage("Created "+new File("Modded "+modderator.gameModule.name+".exe").getAbsolutePath()+", this needs to stay next to Modderator.jar, but you can create shortcuts from it or pin it to your taskbar!");
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (MalformedURLException e3) {
					e3.printStackTrace();
				} catch (IOException e4) {
					e4.printStackTrace();
				}
			}});
		GUIhelpUser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Modderator is designed to easily apply mods to a variety of games.\n"+
						"Mods go in the mods folder and should have the ."+modderator.gameModule.modExtention+" format.\n"+
						"Every time the game updates updates, you should re-run this program and re-apply.\n"+
						"Otherwise some mods may be overridden, or break the game in some cases.\n"+
						"Troubleshooting/bugs: REPO LINK",
						"User help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpDev.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Directions to making a mod:\n"+
						"1. Put your modified files into a .zip, in their correct path along with a mod.txt file with\n"+
						"  name:<name of mod>\n"+
						"  version:<version of mod>\n"+
						"  author:<author of mod>\n"+
						"  category:<category of mod>\n"+
						"  description:<description of mod>\n\n"+
						"2. Add an icon.png to the zip file, this isn't required, but it is recommended.\n"+
						"3. For all your modified files, place the original ones into\n"+
						"  a folder named \"original\" in the zip file, then rename your .zip to ."+modderator.gameModule.modExtention+".\n"+
						"6. run Modderator, check settings->developer mode, and apply the mod.\n"
						+ "  It will ask you if you want to make an official version.\n"
						+ "  Say yes. Put your original mod file somewhere safe and put the mod it made for you into mods.\n"
						+ "7. Check that you can apply your official mod.\n"
						+ "8. Go to REPO LINK, log in and upload your file.\n"
						+ "\n"
						+ "There are other (harder) ways of applying the mods, similar to the HoN modMan\n"
						+ "Go to <forum link> to get more information.",
						"Developer help", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIhelpAbout.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				showMessage(
						"Modderator!\n"
						+ "Developed by Kairus101\n"
						+ "Version: "+modderator.version+"\n"
						+ "Official website: (REPO LINK)\n"
						+ "\n"
						+ "Other community additions:\n"
						+ "Anakonda: Allowing mods to have requirements.\n"
						+ "\n"
						+ "Want to help Modderator progress too? Github->kairus101\n"
						,
						"About Modderator", JOptionPane.PLAIN_MESSAGE);
			}});
		GUIapplyMods.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				modderator.applyMods();
			}});
		GUIdevMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				modderator.isDeveloper = GUIdevMode.isSelected();
				modderator.saveConfig();
			}});
		GUIhelpChangeLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				final JFrame popup = new JFrame();
				popup.setSize(600, 500);
				JTextArea changes = new JTextArea();
				popup.setTitle("Modderator ChangeLog");
				changes.setText(
						  "Version 1.01\n"
						+ "  Refactored majority of code relating to Strife into it's own module"
						+ "Version 1\n"
						+ "  Branched off my Strife modman v1.16"
				);
				popup.add(new JScrollPane(changes));
				changes.setEditable(false);
				popup.setVisible(true);
			}});

		pack();


		tabbedPane.addTab("Activate mods", null, panel1, "activate mods");
		tabbedPane.addTab("Search for mods online", null, panel2, "look for mods");

		tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (table2 == null){
					modderator.populateOnlineModsTable();
					modderator.purgeOnlineModsTable();

					makeTable2Data();

					String[] columnNames = {"Name", "Rating", "Author", "Catagory", "Description"};
					table2 = new OnlineModsTableModel(new DefaultTableModel(table2Data, columnNames));
					sorter2 = new TableRowSorter<DefaultTableModel>((DefaultTableModel) table2.getModel());
					table2.setRowSorter(sorter2);
					//table.set
					table2.setRowHeight(50);
					table2.setPreferredScrollableViewportSize(new Dimension(200, 70));
					table2.setFillsViewportHeight(true);
					table2.setRowHeight(90);
					table2.getColumnModel().getColumn(0).setMinWidth(120);
					table2.getColumnModel().getColumn(4).setMinWidth(220);
					JScrollPane modPanel = new JScrollPane(table2);
					panel2.add(modPanel, BorderLayout.CENTER);
				}
			}
		});

		add(tabbedPane);



		if (modderator.gameModule.warningOnNoExe && !new File("Modded "+modderator.gameModule.name+".exe").exists())
			showMessage("You can easily start the game with mods with the file created by: File->Create Game launcher!");

		setSize(600, 600);
		setVisible(true);
	}
	
	public void gameSelected(String game){
		ClassLoader classLoader = Modderator.class.getClassLoader();
		try {
			modderator.gameModule = (GameModule) classLoader.loadClass("org.Kairus.Modules."+game+"Module").newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		setVisible(false);
		gameSelectPanel.setVisible(false);
		modderator.gameSelected();
		showMainScreen();
	}
	
	public void showGameSelectScreen(){
		gameSelectPanel.setLayout(new GridLayout(0, 2));
		
		for (File f : new File("modules/").listFiles()) {
			final String name = f.getName().substring(0, f.getName().indexOf("."));
			JButton j = new JButton(name);
			j.setHorizontalTextPosition(SwingConstants.CENTER);
			j.setVerticalTextPosition(SwingConstants.BOTTOM);
			j.setIcon(new ImageIcon(f.getAbsolutePath()));
			gameSelectPanel.add(j);
			
			j.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					gameSelected(name);
				}});
        }
		
		add(gameSelectPanel);
		setSize(600, 600);
		setResizable(false);
		setVisible(true);
	}
	
	public void init(){
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEvt) {
				System.exit(0);
		}});
		showGameSelectScreen();
	}

	void makeTable2Data(){
		table2Data = new Object[modderator.numOnlineMods][];
		int i = 0;
		for (onlineModDescription o:modderator.onlineModList){
			if (!o.framework.toLowerCase().equals("true") || modderator.isDeveloper)
				table2Data[i++] = new Object[]{"<html>"+o.name+"</html>", o.rating, o.author, o.category, "<html>"+o.description+"</html>"};
		}
	}

	//DefaultTableModel model1 = new DefaultTableModel(); 
	class ModsTableModel extends JTable {
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable(int row, int column) {

			return column==0?true:false;
		}
		@Override
		public void setValueAt(Object value, int row, int col) {
			tableData[row][col] = value;
			getModel().setValueAt(value, row, col);
		}  
		@Override
		public Class<?> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}
		ModsTableModel(DefaultTableModel model){
			super(model);
			//setModel(model1);
			//super(data, columnNames);
			getColumnModel().getColumn(0).setCellRenderer(new CheckBoxRenderer());

			ListSelectionModel rowSM = getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) return;
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty()) {
						int selectedRow = lsm.getMinSelectionIndex();
						GUImodName.setText(modderator.mods.get(selectedRow).name);
						GUImodAuthor.setText(modderator.mods.get(selectedRow).author);
						GUImodVersion.setText(modderator.mods.get(selectedRow).version);
					}
				}
			});
		}
	}

	private class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		CheckBoxRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
			if ((Boolean)table.getValueAt(row, col)) {
				setBackground(Color.GREEN);
			} else {
				setBackground(Color.RED);
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));
			tableData[row][0] = value != null && ((Boolean) value).booleanValue();
			return this;
		}
	}

	String modDownloadLink = "";
	String modDownloadName = "";
	class OnlineModsTableModel extends JTable {
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column) {
			return false;
		}
		OnlineModsTableModel(DefaultTableModel defaultTableModel){
			super(defaultTableModel);

			ListSelectionModel rowSM = getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) return;
					ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (!lsm.isSelectionEmpty()) {
						int selectedRow = lsm.getMinSelectionIndex();
						modDownloadLink = modderator.onlineModList.get(selectedRow).link;
						modDownloadName = modderator.onlineModList.get(selectedRow).name;
						GUIdownloadMod.setEnabled(true);
					}
				}
			});

		}
	}
	void addToTable(Object[] row){
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(row);
		Object[][] newTableData = new Object[tableData.length+1][];
		int i = 0;
		for (Object[] o: tableData)
			newTableData[i++] = o;
		newTableData[newTableData.length-1]=row;
		tableData = newTableData;
	}
	void removeFromTable2(int i){
		DefaultTableModel model = (DefaultTableModel) table2.getModel();
		model.removeRow(i);
	}
	void removeFromTable1(int i){
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.removeRow(i);
	}

	public void showMessage(String content){
		JOptionPane.showMessageDialog(this, content);
	}

	public void showMessage(String content, String title, int icon){
		JOptionPane.showMessageDialog(this, content, title, icon);
	}

	public int showYesNo(String title, String content){
		return JOptionPane.showConfirmDialog(this, content, title, JOptionPane.YES_NO_OPTION);
	}
}
