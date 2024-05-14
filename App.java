import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Deque;
import java.util.ArrayDeque;

class UndoStack{
    Deque<String> undoDeque = new ArrayDeque<>();
    Deque<String> redoDeque = new ArrayDeque<>();

    String returnTop() {
        return undoDeque.peek();
    }

    void add(String currentText) {
        //redoDeque.clear();
        if (undoDeque.size() > 10) {
            undoDeque.removeFirst();
        }
        undoDeque.push(currentText);
        redoDeque.push(currentText);
        //System.out.println("top of stack: " + undoDeque.peek());
    }

    String undo(String currText) {
        if (!undoDeque.isEmpty()) {
            String res = undoDeque.pop();
            redoDeque.push(currText);
            return res;
        } else {
            //System.out.println("stack empty");
            return "";
        }
        
    }

    String redo() {
        if (!redoDeque.isEmpty()) {
            String res = redoDeque.pop();
            return res;
        } else {
            //System.out.println("redo empty");
        }
        return undoDeque.peek();
    }

}

public class App implements ActionListener, KeyListener {
    private JFrame frame;
    private JTextArea textArea;
    private UndoStack undoRedo;
    private JLabel wordCountLabel;
    private JLabel characterCountLabel;
    private int count = 0, currCount;
    private boolean ctrlPressed = false, zPressed = false, yPressed = false;
    private boolean saved = false;

    private App() {
        undoRedo = new UndoStack();

        frame = new JFrame("Notepad");
        //implement frame.setTitle();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(this);
        fileMenu.add(openItem);
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(this);
        fileMenu.add(newItem);
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        fileMenu.add(saveItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener(this);
        editMenu.add(undoItem);
        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.addActionListener(this);
        editMenu.add(cutItem);
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(this);
        editMenu.add(copyItem);
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(this);
        editMenu.add(pasteItem);
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(this);
        editMenu.add(deleteItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem zoomInItem = new JMenuItem("Zoom In");
        viewMenu.add(zoomInItem);
        JMenuItem zoomOutItem = new JMenuItem("Zoom Out");
        viewMenu.add(zoomOutItem);
        JMenuItem zoomResetItem = new JMenuItem("Reset Zoom");
        viewMenu.add(zoomResetItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);

        frame.setJMenuBar(menuBar);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.addKeyListener(this);
        JScrollPane scrollPane = new JScrollPane(textArea);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        wordCountLabel = new JLabel("Words: 0");
        characterCountLabel = new JLabel("Characters: 0");

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        labelPanel.add(wordCountLabel);
        labelPanel.add(characterCountLabel);

        frame.add(panel, BorderLayout.CENTER);
        frame.add(labelPanel, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
        textArea.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Open")) {
            openFile();
        } else if (command.equals("New")) {
            newFile();
        } else if (command.equals("Save")) {
            saveFile();
        } else if (command.equals("Undo")) {
            undoAction();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currCount == 0) {
            count = 0;
        }
        updateCounts();
        if (currCount - count >= 10) {
            count = currCount;
            undoRedo.add(textArea.getText());
            //System.out.println(undoRedo.returnTop());
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode  = e.getKeyCode();
        if (keyCode == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
        } else if (keyCode == KeyEvent.VK_Z) {
            zPressed = true;
        } else if (keyCode == KeyEvent.VK_Y) {
            yPressed = true;
        }
        shortcut();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode  = e.getKeyCode();
        if (keyCode == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
        } else if (keyCode == KeyEvent.VK_Z) {
            zPressed = false;
        } else if (keyCode == KeyEvent.VK_Y) {
            yPressed = false;
        }
        shortcut();
    }

    private void shortcut() {
        if (ctrlPressed && zPressed && yPressed) {
            //do nothing
        } else if (ctrlPressed && zPressed) {
            undoAction();
        } else if (ctrlPressed && yPressed) {
            redoAction();
        }
    }

    private void updateCounts() {
        String text = textArea.getText();
        String words[] = text.split("\\s");        
        wordCountLabel.setText("Words :" + words.length);
        currCount = text.length();
        characterCountLabel.setText("Characters: "+ currCount);
    }

    private void openFile() {
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                textArea.read(reader, null);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void newFile() {
        int response = JOptionPane.showConfirmDialog(frame,
            "Do you want to save this file before exiting?",
            "Confirm save",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);    
        
        if (response == JOptionPane.YES_OPTION) {
            saveFile();
        } else if (response == JOptionPane.NO_OPTION) {
            textArea.setText("");
        } else if (response == JOptionPane.CANCEL_OPTION) {
            return;
        }
    }

    private void saveFile() {
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showSaveDialog(frame);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(frame, 
                    "This file already exists, do you want to overwrite it?", 
                    "Confirm overwrite", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    return;
                }   
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                textArea.write(writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void undoAction() {
        textArea.setText(undoRedo.undo(textArea.getText()));
    }

    private void redoAction() {
        textArea.setText(undoRedo.redo());
    }
}
