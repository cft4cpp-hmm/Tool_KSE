package GUI;

import HybridAutoTestGen.CFT4CPP;
import HybridAutoTestGen.FullBoundedTestGen;
import HybridAutoTestGen.WeightedCFGTestGEn;
import config.Paths;
import console.Console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Frame {

	private JFrame frmWcftcpp;
	private final ButtonGroup buttonGroup1 = new ButtonGroup();
	private JTextField funcName;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frame window = new Frame();
					window.frmWcftcpp.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWcftcpp = new JFrame();
		frmWcftcpp.setTitle("WCFT4Cpp");
		frmWcftcpp.setBounds(100, 100, 747, 431);
		frmWcftcpp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmWcftcpp.getContentPane().setLayout(null);
		
		JRadioButton WCFT4Cpp = new JRadioButton("WCFG");
		WCFT4Cpp.setSelected(true);
		JRadioButton BCFT4Cpp = new JRadioButton("BVTG");
		JRadioButton STCFG = new JRadioButton("STCFG");
		JRadioButton CTCFG = new JRadioButton("Concolic");
		JTextPane txtpnP = new JTextPane();
		JComboBox comboBox = new JComboBox();
		comboBox.addItem(0);
		comboBox.addItem(1);
		comboBox.addItem(2);
		comboBox.addItem(3);
		comboBox.addItem(4);
		comboBox.setSelectedIndex(0);
		comboBox.setBounds(196, 132, 56, 21);
		frmWcftcpp.getContentPane().add(comboBox);
		
		
		
		STCFG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtpnP.setText("Please fill out required feild and wait until every thing done");
				txtpnP.setForeground(Color.black);
				if(STCFG.isSelected()) {
					BCFT4Cpp.setSelected(false);
					WCFT4Cpp.setSelected(false);
					CTCFG.setSelected(false);
				}
			}
		});
		
		
		JButton btnStart = new JButton("Generate test data");
		
		
		JLabel lblFunctionNamw = new JLabel("Function Name");
		WCFT4Cpp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtpnP.setText("Please fill out required feild and wait until every thing done");
				txtpnP.setForeground(Color.black);
				if(WCFT4Cpp.isSelected()) {
					BCFT4Cpp.setSelected(false);
					CTCFG.setSelected(false);
					STCFG.setSelected(false);
				}
			}
		});
		buttonGroup1.add(WCFT4Cpp);
		buttonGroup1.add(BCFT4Cpp);
		buttonGroup1.add(CTCFG);
		buttonGroup1.add(STCFG);
		WCFT4Cpp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		WCFT4Cpp.setBounds(61, 40, 137, 21);
		frmWcftcpp.getContentPane().add(WCFT4Cpp);
		
		BCFT4Cpp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtpnP.setText("Please fill out required feild and wait until every thing done");
				txtpnP.setForeground(Color.black);
				if(BCFT4Cpp.isSelected()) {
					WCFT4Cpp.setSelected(false);
					CTCFG.setSelected(false);
					STCFG.setSelected(false);
				}
			}
		});
		BCFT4Cpp.setFont(new Font("Tahoma", Font.PLAIN, 14));
		BCFT4Cpp.setBounds(61, 90, 131, 21);
		frmWcftcpp.getContentPane().add(BCFT4Cpp);
		CTCFG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtpnP.setText("Please fill out required feild and wait until every thing done");
				txtpnP.setForeground(Color.black);
				if(CTCFG.isSelected()) {
					BCFT4Cpp.setSelected(false);
					WCFT4Cpp.setSelected(false);
					STCFG.setSelected(false);
				}
			}
		});
		STCFG.setFont(new Font("Tahoma", Font.PLAIN, 14));
		STCFG.setBounds(212, 40, 249, 21);
		frmWcftcpp.getContentPane().add(STCFG);

		CTCFG.setFont(new Font("Tahoma", Font.PLAIN, 14));
		CTCFG.setBounds(214, 90, 94, 21);
		frmWcftcpp.getContentPane().add(CTCFG);
		
		JLabel depthLabel = new JLabel("Depth");
		depthLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		depthLabel.setBounds(61, 127, 49, 27);
		frmWcftcpp.getContentPane().add(depthLabel);
		

		

		funcName = new JTextField();
		funcName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		funcName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtpnP.setText("Please fill out required feild and wait until every thing done");
				txtpnP.setForeground(Color.black);
				txtpnP.setForeground(Color.black);
			}
		});
		
		funcName.setColumns(10);
		funcName.setBounds(196, 164, 187, 22);
		frmWcftcpp.getContentPane().add(funcName);
		
		txtpnP.setFont(new Font("Verdana", Font.BOLD, 14));
		txtpnP.setBackground(SystemColor.control);
		txtpnP.setForeground(Color.BLACK);
		txtpnP.setText("Please wait until every thing done");
		txtpnP.setBounds(35, 305, 426, 27);
		txtpnP.setEditable(false);
		frmWcftcpp.getContentPane().add(txtpnP);
		
		
		btnStart.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
//				.setForeground(Color.blue);	
//				txtpnP.setText("Waiting....");
				
				
//				String pathToz3 = Z3.getText();
//				String pathToMingw32 = mingw.getText();
//				String pathToGcc = gcc.getText();
//				String pathTogPlus = gplus.getText();
//				
//				Main.pathToZ3 = pathToz3;
//				Main.pathToGCC = pathToGcc;
//				Main.pathToMingw32 = pathToMingw32;
//				Main.pathToGPlus = pathTogPlus;

				try {
					int iterations = (int) comboBox.getSelectedItem();
					String functionName = funcName.getText();
					CFT4CPP cft4cpp = new CFT4CPP(null,iterations, Paths.TSDV_R1_2, functionName);
					WeightedCFGTestGEn weightedCFGTestGEn = new WeightedCFGTestGEn(functionName, iterations,Paths.TSDV_R1_2);
					FullBoundedTestGen bGen = new FullBoundedTestGen(iterations, functionName, Paths.TSDV_R1_2);
					if(STCFG.isSelected()) {
						cft4cpp.run();
					}
					else if(WCFT4Cpp.isSelected()) {
						weightedCFGTestGEn.run();
					}
					else if(BCFT4Cpp.isSelected()) {
						bGen.boundaryValueTestGen();
					}
					else if(CTCFG.isSelected()) {
						Console console = new Console(functionName);
					}
					txtpnP.setText("open report.html to view the result!");
					txtpnP.setForeground(Color.blue);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					txtpnP.setText("Error while running tool. Please reivse your inputs.");
					txtpnP.setForeground(Color.red);
					e1.printStackTrace();
				}
				
//				try {
//					main.run();
//					txtpnP.setText("open report.html to view the result!");
//				}catch (Exception ex) {
//					// TODO: handle exception
//					txtpnP.setText("Error while running tool. Please reivse your inputs.");
//				}
			}
		});
		
		btnStart.setFont(new Font("Verdana", Font.PLAIN, 13));
		btnStart.setBounds(212, 203, 170, 27);
		frmWcftcpp.getContentPane().add(btnStart);
		
		
		lblFunctionNamw.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblFunctionNamw.setBounds(61, 164, 117, 27);
		frmWcftcpp.getContentPane().add(lblFunctionNamw);
		
		
		
		
	}
}
