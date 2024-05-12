import javax.swing.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class undoStack {
    Deque<String> undoDeque = new ArrayDeque<>();
    Deque<String> redoDeque = new ArrayDeque<>();

    String returnTop() {
        return undoDeque.peek();
    }

    void add(String currentText) {
        redoDeque.clear();
        if (undoDeque.size() > 10) {
            undoDeque.removeFirst();
        }
        undoDeque.add(currentText);
    }

    String undo() {
        redoDeque.add(undoDeque.pop());
        return undoDeque.peek();
    }

    String redo() {
        if (!redoDeque.isEmpty()) {
            return redoDeque.peek();
        }
        return undoDeque.peek();
    }

}

public class App implements ActionListener, KeyListener {
    JMenu File, Edit, View, View_subMenu;
    JMenuItem i1_1, i1_2, i1_3;
    JMenuItem i2_1, i2_2, i2_3, i2_4, i2_5;
    JMenuItem i3_1, i31_1, i3_1_2, i3_1_3;
    JTextArea area;
    JLabel wordCount, characterCount;
    JFrame f;
    int count = 0, currCount;
    undoStack undoRedo;

    App () {
        undoRedo = new undoStack();

        f = new JFrame("Notepad");

        JMenuBar mb = new JMenuBar();
        File = new JMenu("File");
        Edit = new JMenu("Edit");
        View = new JMenu("View");
        View_subMenu = new JMenu("Zoom");

        i1_1 = new JMenuItem("Open");
        i1_2 = new JMenuItem("New");
        i1_3 = new JMenuItem("Save");

        i1_1.addActionListener(this);
        i1_2.addActionListener(this);
        i1_3.addActionListener(this);
        
        File.add(i1_1);
        File.add(i1_2);
        File.add(i1_3);

        i2_1 = new JMenuItem("Undo");
        i2_2 = new JMenuItem("Cut");
        i2_3 = new JMenuItem("Copy");
        i2_4 = new JMenuItem("Paste");
        i2_5 = new JMenuItem("Delete");

        i2_1.addActionListener(this);

        Edit.add(i2_1);
        Edit.add(i2_2);
        Edit.add(i2_3);
        Edit.add(i2_4);
        Edit.add(i2_5);

        i3_1 = new JMenuItem("Zoom");
        i31_1 = new JMenuItem("Zoom in");
        i3_1_2 = new JMenuItem("Zoom out");
        i3_1_3 = new JMenuItem("Reset Zoom");

        //View.add(i3_1);
        View_subMenu.add(i31_1);
        View_subMenu.add(i3_1_2);
        View_subMenu.add(i3_1_3);
        View.add(View_subMenu);

        mb.add(File);
        mb.add(Edit);
        mb.add(View);

        area = new JTextArea();
        area.setBounds(0, 0, 600, 600); 
        area.addKeyListener(this);

        wordCount = new JLabel("Words: 0");
        wordCount.setBounds(10, 605, 150, 10);
        characterCount = new JLabel("Characters : 0");
        characterCount.setBounds(120, 605, 150, 10);

        f.setSize(600, 680);
        f.setJMenuBar(mb);
        f.setLayout(null);

        f.add(area);
        f.add(wordCount); 
        f.add(characterCount);
        
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == i1_1) {
            openFile();
        } else if (e.getSource() == i1_2) {
            newFile();
        }else if (e.getSource() == i1_3) {
            saveFile();
        }else if (e.getSource() == i2_1) {
            undoAction();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        updateCounts();
        if (currCount - count >= 10) {
            count = currCount;
            undoRedo.add(area.getText());
            System.out.println(undoRedo.returnTop());
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private void updateCounts() {
        String text = area.getText();
        String words[] = text.split("\\s");        
        wordCount.setText("Words :" + words.length);
        currCount = text.length();
        characterCount.setText("Characters: "+ currCount);
    }

    private void openFile() {
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showOpenDialog(f);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                area.read(reader, null);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void newFile() {
        int response = JOptionPane.showConfirmDialog(f,
            "Do you want to save this file before exiting?",
            "Confirm save",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (response == JOptionPane.YES_OPTION) {
            saveFile();
        } else if (response == JOptionPane.NO_OPTION) {
            area.setText("");
        } else if (response == JOptionPane.CANCEL_OPTION) {
            return;
        }
    }

    private void saveFile() {
        JFileChooser jfc = new JFileChooser();
        int returnValue = jfc.showSaveDialog(f);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(f, 
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
                area.write(writer);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void undoAction() {
        area.setText(undoRedo.undo());
    }
    public static void main(String[] args) throws Exception {
        new App();
    }
}
