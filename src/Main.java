import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {
    private static final int MATRIX_SIZE = 5;
    private char[][] keyMatrix = new char[MATRIX_SIZE][MATRIX_SIZE];
    private String keyword;

    private JTextField keywordField;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JTextArea matrixDisplayArea;

    public Main() {
        setTitle("Playfair Cipher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JPanel centerLeftPanel = new JPanel();
        JPanel centerMiddlePanel = new JPanel();
        JPanel centerRightPanel = new JPanel();

        centerLeftPanel.setLayout(new BoxLayout(centerLeftPanel, BoxLayout.Y_AXIS));
        centerMiddlePanel.setLayout(new BoxLayout(centerMiddlePanel, BoxLayout.Y_AXIS));
        centerRightPanel.setLayout(new BoxLayout(centerRightPanel, BoxLayout.Y_AXIS));

        topPanel.add(new JLabel("Keyword:"));
        keywordField = new JTextField(20);
        topPanel.add(keywordField);

        inputTextArea = new JTextArea(15, 30);
        inputTextArea.setText("Enter text here");
        inputTextArea.setForeground(Color.GRAY);
        inputTextArea.setLineWrap(true);
        inputTextArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputTextArea.getText().equals("Enter text here")) {
                    inputTextArea.setText("");
                    inputTextArea.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (inputTextArea.getText().isEmpty()) {
                    inputTextArea.setText("Enter text here");
                    inputTextArea.setForeground(Color.GRAY);
                }
            }
        });

        outputTextArea = new JTextArea(15, 30);
        outputTextArea.setEditable(false);
        outputTextArea.setLineWrap(true);
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        matrixDisplayArea = new JTextArea(15, 30);
        matrixDisplayArea.setEditable(false);
        matrixDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 20));

        centerLeftPanel.add(new JLabel("Key Matrix:"));
        centerMiddlePanel.add(new JLabel("Input Text:"));
        centerRightPanel.add(new JLabel("Output Text: "));

        centerLeftPanel.add(new JScrollPane(matrixDisplayArea));
        centerMiddlePanel.add(new JScrollPane(inputTextArea));
        centerRightPanel.add(new JScrollPane(outputTextArea));

        centerLeftPanel.add(Box.createVerticalStrut(10));
        centerMiddlePanel.add(Box.createVerticalStrut(10));
        centerRightPanel.add(Box.createVerticalStrut(10));

        centerPanel.add(centerLeftPanel);
        centerPanel.add(centerMiddlePanel);
        centerPanel.add(centerRightPanel);

        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");

        encryptButton.addActionListener(e -> processText(true));
        decryptButton.addActionListener(e -> processText(false));

        bottomPanel.add(encryptButton);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(decryptButton);

        // Add all panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
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

    private void processText(boolean isEncrypting) {
        try {
            String input = inputTextArea.getText().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "K");

            if (isEncrypting) {
                String encryptedText = encrypt(input);
                outputTextArea.setText(encryptedText);
            } else {
                String decryptedText = decrypt(input);
                Set<String> possibilities = getAllPossibleTexts(decryptedText);

                StringBuilder output = new StringBuilder();
                output.append("All possible variations:\n");
                int count = 1;

                List<String> sortedPossibilities = new ArrayList<>(possibilities);
                Collections.sort(sortedPossibilities);

                for (String text : sortedPossibilities) {
                    output.append(count++).append(". ").append(text).append("\n");
                }
                outputTextArea.setText(output.toString());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing text: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateMatrix() {
        keyword = keywordField.getText().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "K");
        getMatrixWithKey();
        displayMatrix();
    }

    private void displayMatrix() {
        StringBuilder matrixStr = new StringBuilder();
        for (int i = 0; i < MATRIX_SIZE; i++) {
            for (int j = 0; j < MATRIX_SIZE; j++) {
                matrixStr.append(keyMatrix[i][j]).append("   ");
            }
            matrixStr.append("\n\n");
        }
        matrixDisplayArea.setText(matrixStr.toString());
    }

    private Set<String> getPossiblePQVariations(String text) {
        Set<String> variations = new HashSet<>();
        variations.add(text);

        char[] chars = text.toCharArray();

        List<Integer> pPositions = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 'K') {
                pPositions.add(i);
            }
        }

        int n = pPositions.size();
        for (int i = 1; i < (1 << n); i++) {
            char[] newVariation = text.toCharArray();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    newVariation[pPositions.get(j)] = 'J';
                }
            }
            variations.add(new String(newVariation));
        }

        return variations;
    }

    private Set<String> getPossibleTexts(String decryptedText) {
        Set<String> possibilities = new HashSet<>();
        possibilities.add(decryptedText);

        if (decryptedText.endsWith("X")) {
            possibilities.add(decryptedText.substring(0, decryptedText.length() - 1));
        }

        StringBuilder temp = new StringBuilder(decryptedText);
        for (int i = 0; i < temp.length() - 2; i++) {
            if (temp.charAt(i + 1) == 'X' && temp.charAt(i) == temp.charAt(i + 2)) {
                String possibility = temp.substring(0, i + 1) + temp.substring(i + 2);
                possibilities.add(possibility);
            }
        }

        return possibilities;
    }

    private Set<String> getAllPossibleTexts(String decryptedText) {
        Set<String> possibilities = getPossibleTexts(decryptedText);
        Set<String> allPossibilities = new HashSet<>();
        for (String text : possibilities) {
            allPossibilities.addAll(getPossiblePQVariations(text));
        }

        return allPossibilities;
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