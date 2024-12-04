import java.util.Scanner;

public class Main {
    private static final int MATRIX_SIZE = 5;
    private char[][] keyMatrix = new char[MATRIX_SIZE][MATRIX_SIZE];
    private String keyword;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Main cipher = new Main();

        System.out.print("Please Enter a Keyword: ");
        cipher.keyword = scanner.nextLine().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");

        cipher.getMatrixWithKey();

        System.out.println("Do you want encrypt or decrypt? (Enter the number):");
        System.out.println("1. Encrypt");
        System.out.println("2. Decrypt");

        try{
            int choice = scanner.nextInt();
            scanner.nextLine();

            System.out.print("Enter text: ");
            String plaintext = scanner.nextLine().toUpperCase().replaceAll("[^A-Z]", "").replace("J", "I");

            if (choice == 1) {
                System.out.println("Encrypted text: " + cipher.encrypt(plaintext));
            } else if (choice == 2) {
                System.out.println("Decrypted text: " + cipher.decrypt(plaintext));
            } else {
                System.out.println("Invalid choice.");
            }
        }catch(Exception e){
            System.out.println("Invalid Input");
            System.exit(0);
        }

        scanner.close();
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
}
