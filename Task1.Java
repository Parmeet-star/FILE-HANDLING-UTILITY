import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TextFileEditor extends JFrame implements ActionListener {
    private JTextArea textArea;
    private JMenuItem openMenuItem, saveMenuItem, saveAsMenuItem, clearMenuItem, exitMenuItem;
    private File currentFile = null;
    private JLabel statusBar;
    private boolean isModified = false;
    private File lastDirectory = null;

    public TextFileEditor() {
        setTitle("Text File Editor");
        setSize(600, 500);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Status bar
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        // Menu bar and menus
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");

        openMenuItem = new JMenuItem("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveAsMenuItem = new JMenuItem("Save As");
        clearMenuItem = new JMenuItem("Clear");
        clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        exitMenuItem = new JMenuItem("Exit");

        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveAsMenuItem.addActionListener(this);
        clearMenuItem.addActionListener(this);
        exitMenuItem.addActionListener(this);

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        editMenu.add(clearMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Track modifications
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { setModified(true); }
        });

        // Confirm on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitEditor();
            }
        });

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == openMenuItem) {
            openFile();
        } else if (src == saveMenuItem) {
            saveFile(false);
        } else if (src == saveAsMenuItem) {
            saveFile(true);
        } else if (src == clearMenuItem) {
            if (!textArea.getText().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this, "Clear all text?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    textArea.setText("");
                    setModified(true);
                }
            }
        } else if (src == exitMenuItem) {
            exitEditor();
        }
    }

    private void openFile() {
        if (isModified && !confirmSave()) return;

        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            lastDirectory = currentFile.getParentFile();
            try {
                // Open file in Chrome (or default browser)
                Desktop.getDesktop().browse(currentFile.toURI());
                statusBar.setText("Opened in browser: " + currentFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file in browser: " + ex.getMessage());
                statusBar.setText("Error opening file in browser");
            }
        }
    }

    private void saveFile(boolean saveAs) {
        if (currentFile == null || saveAs) {
            JFileChooser fileChooser = new JFileChooser(lastDirectory);
            int result = fileChooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                lastDirectory = currentFile.getParentFile();
            } else {
                return; // User canceled
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(writer);
            setModified(false);
            statusBar.setText("Saved: " + currentFile.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            statusBar.setText("Error saving file");
        }
    }

    private void exitEditor() {
        if (isModified && !confirmSave()) return;
        dispose();
    }

    private boolean confirmSave() {
        int option = JOptionPane.showConfirmDialog(this, "You have unsaved changes. Save now?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
        if (option == JOptionPane.CANCEL_OPTION) return false;
        if (option == JOptionPane.YES_OPTION) saveFile(false);
        return true;
    }

    private void setModified(boolean modified) {
        isModified = modified;
        setTitle((modified ? "*" : "") + "Text File Editor");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextFileEditor::new);
    }
}
