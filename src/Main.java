import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {
    private static final int MATRIX_SIZE = 5;
    private char[][] keyMatrix = new char[MATRIX_SIZE][MATRIX_SIZE];
    private String keyword;

    private JTextField keywordField;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JTextArea matrixDisplayArea;
    private JRadioButton encryptButton;
    private JRadioButton decryptButton;

    public Main() {
        setTitle("Playfair Cipher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Create panels
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // Keyword input
        topPanel.add(new JLabel("Keyword:"));
        keywordField = new JTextField(20);
        topPanel.add(keywordField);

        // Radio buttons for mode selection
        encryptButton = new JRadioButton("Encrypt", true);
        decryptButton = new JRadioButton("Decrypt");
        ButtonGroup group = new ButtonGroup();
        group.add(encryptButton);
        group.add(decryptButton);
        JPanel radioPanel = new JPanel();
        radioPanel.add(encryptButton);
        radioPanel.add(decryptButton);
        topPanel.add(radioPanel);

        // Text areas
        inputTextArea = new JTextArea(10, 30);
        outputTextArea = new JTextArea(10, 30);
        outputTextArea.setEditable(false);
        matrixDisplayArea = new JTextArea(20, 30);
        matrixDisplayArea.setEditable(false);

        matrixDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 20));

        inputTextArea.setText("Enter text here");

        centerPanel.add(new JLabel("Input Text:"));
        centerPanel.add(new JLabel("Output Text:"));
        centerPanel.add(new JScrollPane(inputTextArea));
        centerPanel.add(new JScrollPane(outputTextArea));

        // Matrix display panel
        JPanel matrixPanel = new JPanel(new BorderLayout());
        matrixPanel.add(new JLabel("Key Matrix:"), BorderLayout.NORTH);
        matrixPanel.add(new JScrollPane(matrixDisplayArea), BorderLayout.CENTER);

        matrixPanel.setSize(200, 200);

        // Process button
        JButton processButton = new JButton("Process");
        processButton.addActionListener(e -> processText());
        bottomPanel.add(processButton);

        // Add all panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(matrixPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Add key listener for keyword field
        keywordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateMatrix();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void updateMatrix() {
        keyword = keywordField.getText().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");
        getMatrixWithKey();
        displayMatrix();
    }

    private void displayMatrix() {
        StringBuilder matrixStr = new StringBuilder();
        for (int i = 0; i < MATRIX_SIZE; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                matrixStr.append(keyMatrix[i][j]).append(" ");
            }
            matrixStr.append("\n");
        }
        matrixDisplayArea.setText(matrixStr.toString());
    }

    private void processText() {
        try {
            String input = inputTextArea.getText().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");
            String result;

            if (encryptButton.isSelected()) {
                result = encrypt(input);
            } else {
                result = decrypt(input);
            }

            outputTextArea.setText(result);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing text: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void getMatrixWithKey() {
        boolean[] used = new boolean[26];
        int index = 0;

        for (char c : keyword.toCharArray()) {
            if (!used[c - 'A']) {
                keyMatrix[index / MATRIX_SIZE][index % MATRIX_SIZE] = c;
                used[c - 'A'] = true;
                index++;
            }
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            if (c == 'J') continue;
            if (!used[c - 'A']) {
                keyMatrix[index / MATRIX_SIZE][index % MATRIX_SIZE] = c;
                used[c - 'A'] = true;
                index++;
            }
        }
    }

    private String textProcess(String text, boolean isEncrypting) {
        StringBuilder processedText = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c1 = text.charAt(i);
            char c2 = (i + 1 < text.length()) ? text.charAt(i + 1) : 'X';

            processedText.append(c1);

            if (c1 == c2) {
                processedText.append('X');
            } else {
                i++;
                processedText.append(c2);
            }
        }

        if (processedText.length() % 2 != 0) {
            processedText.append('X');
        }

        return isEncrypting ? processedText.toString() : text;
    }

    private String encrypt(String plaintext) {
        plaintext = textProcess(plaintext, true);
        return pairsProcess(plaintext, true);
    }

    private String decrypt(String ciphertext) {
        return pairsProcess(ciphertext, false);
    }

    private String pairsProcess(String text, boolean isEncrypting) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i += 2) {
            char c1 = text.charAt(i);
            char c2 = text.charAt(i + 1);

            int[] pos1 = findPlace(c1);
            int[] pos2 = findPlace(c2);

            if (pos1[0] == pos2[0]) {
                result.append(keyMatrix[pos1[0]][(pos1[1] + (isEncrypting ? 1 : 4)) % MATRIX_SIZE]);
                result.append(keyMatrix[pos2[0]][(pos2[1] + (isEncrypting ? 1 : 4)) % MATRIX_SIZE]);
            } else if (pos1[1] == pos2[1]) {
                result.append(keyMatrix[(pos1[0] + (isEncrypting ? 1 : 4)) % MATRIX_SIZE][pos1[1]]);
                result.append(keyMatrix[(pos2[0] + (isEncrypting ? 1 : 4)) % MATRIX_SIZE][pos2[1]]);
            } else {
                result.append(keyMatrix[pos1[0]][pos2[1]]);
                result.append(keyMatrix[pos2[0]][pos1[1]]);
            }
        }

        return result.toString();
    }

    private int[] findPlace(char c) {
        for (int row = 0; row < MATRIX_SIZE; row++) {
            for (int col = 0; col < MATRIX_SIZE; col++) {
                if (keyMatrix[row][col] == c) {
                    return new int[]{row, col};
                }
            }
        }
        throw new IllegalArgumentException("Character not found in key matrix: " + c);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}