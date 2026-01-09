package ghidrassist.ui.tabs;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import ghidra.util.Msg;
import ghidrassist.core.TabController;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

public class ImprovedDecompilationTab extends JPanel {
    private static final long serialVersionUID = 1L;
    private final TabController controller;
    private RSyntaxTextArea codeTextArea;
    private JButton improveDecompilationButton;
    private JButton clearButton;
    private JButton saveButton;

    public ImprovedDecompilationTab(TabController controller) {
        super(new BorderLayout());
        this.controller = controller;
        initializeComponents();
        layoutComponents();
        setupListeners();
        setupContextMenu();
    }

    /**
     * Check if a color is dark (for determining light vs dark mode).
     */
    private boolean isColorDark(Color color) {
        if (color == null)
            return false;
        // Use perceived brightness formula
        double brightness = (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000.0;
        return brightness < 128;
    }

    private void initializeComponents() {
        // Initialize text area for Code viewing/editing
        codeTextArea = new RSyntaxTextArea(20, 60);
        codeTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        codeTextArea.setCodeFoldingEnabled(true);
        codeTextArea.setAntiAliasingEnabled(true);
        codeTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        codeTextArea.setEditable(true); // Always editable for now, or we can toggle

        Color panelBg = UIManager.getColor("Panel.background");
        if (isColorDark(panelBg)) {
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(codeTextArea);
            } catch (Exception e) {
                Msg.error(this, "Failed to load theme: " + e.getMessage());
            }
        }

        // Initialize buttons
        improveDecompilationButton = new JButton("Improve Decompilation");
        clearButton = new JButton("Clear");
        saveButton = new JButton("Save Changes");
        saveButton.setEnabled(false); // Enable only when content changes or is loaded?
        // Actually simpler to just let valid save whenever.
        saveButton.setEnabled(true);
    }

    private void layoutComponents() {
        // Toolbar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(improveDecompilationButton);
        topPanel.add(saveButton);
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);

        // Code area
        add(new RTextScrollPane(codeTextArea), BorderLayout.CENTER);
    }

    private void setupListeners() {
        improveDecompilationButton.addActionListener(e -> controller.handleImproveDecompilation());

        clearButton.addActionListener(e -> {
            codeTextArea.setText("");
            // Optional: clear from DB too?
            // controller.handleClearImprovedDecompilation();
        });

        saveButton.addActionListener(e -> {
            String content = codeTextArea.getText();
            controller.handleUpdateImprovedDecompilation(content);
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (codeTextArea.getText().trim().isEmpty()) {
                    controller.handleImproveDecompilation();
                }
            }
        });
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(e -> {
            String selectedText = codeTextArea.getSelectedText();
            if (selectedText == null || selectedText.isEmpty()) {
                selectedText = codeTextArea.getText();
            }
            if (selectedText != null && !selectedText.isEmpty()) {
                copyToClipboard(selectedText);
            }
        });

        JMenuItem paste = new JMenuItem("Paste");
        paste.addActionListener(e -> codeTextArea.paste());

        contextMenu.add(copy);
        contextMenu.add(paste);

        codeTextArea.setComponentPopupMenu(contextMenu);
    }

    private void copyToClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(text), null);
            } catch (Exception e) {
                Msg.error(this, "Failed to copy to clipboard: " + e.getMessage());
            }
        }
    }

    public void setCodeText(String text) {
        codeTextArea.setText(text);
        codeTextArea.setCaretPosition(0);
    }

    public String getCodeText() {
        return codeTextArea.getText();
    }
}
