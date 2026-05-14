package org.antredesloutres.papi.service.image;

import lombok.RequiredArgsConstructor;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.model.translation.TypeTranslation;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageGeneratorService {

    private static final String TEMPLATE_PATH = "template/pokemon_summary.png";

    public BufferedImage generatePkmnInfoImage(Pkmn pkmn, Language language) {
        BufferedImage template;
        try {
            template = ImageIO.read(new File(TEMPLATE_PATH));
        } catch (IOException e) {
            template = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = template.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, 1920, 1080);
            g.dispose();
        }

        int width = template.getWidth();
        int height = template.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.drawImage(template, 0, 0, null);

        double sw = width / 1920.0;
        double sh = height / 1080.0;

        // Middle Top Bar: Name
        drawName(g, pkmn, language, (int)(780 * sw), (int)(100 * sh), (int)(640 * sw), (int)(120 * sh), sw, sh);

        // Center Circle: Sprite (smaller)
        drawSprite(g, pkmn.getSpriteUrl(), (int)(850 * sw), (int)(250 * sh), (int)(500 * sw), (int)(550 * sh));

        // Middle Bottom Bar: Types
        drawTypes(g, pkmn, language, (int)(780 * sw), (int)(860 * sh), (int)(640 * sw), (int)(120 * sh), sw, sh);

        // Top Left Box: Stats
        drawStats(g, pkmn, (int)(100 * sw), (int)(100 * sh), (int)(610 * sw), (int)(420 * sh), sw, sh);

        // Bottom Left Box: Abilities (centered)
        drawAbilities(g, pkmn, language, (int)(100 * sw), (int)(560 * sh), (int)(610 * sw), (int)(420 * sh), sw, sh);

        // Top Right Box: Info (Height, Weight, Experience)
        drawInfo(g, pkmn, language, (int)(1480 * sw), (int)(100 * sh), (int)(360 * sw), (int)(520 * sh), sw, sh);

        // Bottom Right Circle: National Dex Number
        drawDexNumber(g, pkmn.getNationalDexNumber(), (int)(1540 * sw), (int)(660 * sh), (int)(300 * sw), (int)(300 * sh), sw, sh);

        g.dispose();
        return image;
    }

    private void drawName(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        String name = getPkmnName(pkmn, language).toUpperCase();
        String form = getFormName(pkmn, language);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(48 * sw)));
        FontMetrics fm = g.getFontMetrics();
        
        int textY = y + (h / 2) + (fm.getAscent() / 2) - (int)(5 * sh);
        if (form != null && !form.equalsIgnoreCase("normal")) {
            textY -= (int)(15 * sh);
            g.drawString(name, x + (w - fm.stringWidth(name)) / 2, textY);
            
            g.setFont(new Font("SansSerif", Font.ITALIC, (int)(24 * sw)));
            fm = g.getFontMetrics();
            g.drawString("(" + form + ")", x + (w - fm.stringWidth("(" + form + ")")) / 2, textY + (int)(35 * sh));
        } else {
            g.drawString(name, x + (w - fm.stringWidth(name)) / 2, textY);
        }
    }

    private void drawTypes(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        List<Type> types = new ArrayList<>();
        types.add(pkmn.getPrimaryType());
        if (pkmn.getSecondaryType() != null) types.add(pkmn.getSecondaryType());

        int typeWidth = (int)(160 * sw);
        int typeHeight = (int)(50 * sh);
        int typeGap = (int)(20 * sw);
        int totalTypesWidth = types.size() * typeWidth + (types.size() - 1) * typeGap;
        int startX = x + (w - totalTypesWidth) / 2;
        int typeY = y + (h - typeHeight) / 2;

        for (int i = 0; i < types.size(); i++) {
            Type t = types.get(i);
            if (t == null) continue;
            
            Color typeColor = parseColor(t.getColor(), new Color(168, 168, 120));
            g.setColor(typeColor);
            g.fillRoundRect(startX + i * (typeWidth + typeGap), typeY, typeWidth, typeHeight, 15, 15);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, (int)(22 * sw)));
            String typeName = getTypeName(t, language).toUpperCase();
            FontMetrics tfm = g.getFontMetrics();
            g.drawString(typeName, startX + i * (typeWidth + typeGap) + (typeWidth - tfm.stringWidth(typeName)) / 2, typeY + (int)(34 * sh));
        }
    }

    private void drawInfo(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(28 * sw)));
        int currentY = y + (int)(80 * sh);
        int labelX = x + (int)(40 * sw);
        int valueX = x + w - (int)(40 * sw);

        String[][] info = {
            {"Taille", String.format("%.1f m", pkmn.getHeight() / 100.0)},
            {"Poids", String.format("%.1f kg", pkmn.getWeight() / 10.0)},
            {"Exp Base", String.valueOf(pkmn.getExperienceYield())},
            {"Capture", String.valueOf(pkmn.getCatchRate())}
        };

        for (String[] item : info) {
            g.setFont(new Font("SansSerif", Font.BOLD, (int)(22 * sw)));
            g.drawString(item[0], labelX, currentY);
            
            g.setFont(new Font("SansSerif", Font.PLAIN, (int)(22 * sw)));
            int valW = g.getFontMetrics().stringWidth(item[1]);
            g.drawString(item[1], valueX - valW, currentY);
            
            currentY += (int)(60 * sh);
        }
        
        // Symbol at the bottom of the box
        g.setColor(new Color(0, 0, 0, 80));
        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(20 * sw)));
        String symbol = pkmn.getSymbol();
        int symW = g.getFontMetrics().stringWidth(symbol);
        g.drawString(symbol, x + (w - symW) / 2, y + h - (int)(40 * sh));
    }

    private void drawDexNumber(Graphics2D g, Integer dexNum, int x, int y, int w, int h, double sw, double sh) {
        if (dexNum == null) return;
        String text = String.format("#%04d", dexNum);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(56 * sw)));
        g.setColor(new Color(50, 50, 50));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + (h / 2) + (fm.getAscent() / 2));
    }

    private void drawStats(Graphics2D g, Pkmn pkmn, int x, int y, int w, int h, double sw, double sh) {
        String[] labels = {"PV", "Atk", "Def", "Atk Sp", "Def Sp", "Vit"};
        int[] values = {
                pkmn.getBaseHp(), pkmn.getBaseAttack(), pkmn.getBaseDefense(),
                pkmn.getBaseSpeAttack(), pkmn.getBaseSpeDefense(), pkmn.getBaseSpeed()
        };
        
        Color[] colors = {
            new Color(255, 89, 89),  // HP
            new Color(240, 128, 48), // Atk
            new Color(248, 208, 48), // Def
            new Color(104, 144, 240),// SpAtk
            new Color(120, 200, 80), // SpDef
            new Color(248, 88, 136)  // Speed
        };

        int paddingX = (int)(40 * sw);
        int currentY = y + (int)(80 * sh);
        int labelWidth = (int)(100 * sw);
        int barX = x + paddingX + labelWidth;
        int barMaxWidth = w - (int)(240 * sw); // Adjusted to avoid overflow
        int barHeight = (int)(22 * sh);
        int spacing = (int)(55 * sh);

        g.setFont(new Font("SansSerif", Font.BOLD, (int)(22 * sw)));
        
        for (int i = 0; i < labels.length; i++) {
            g.setColor(Color.BLACK);
            g.drawString(labels[i], x + paddingX, currentY + (int)(18 * sh));

            // Bar background
            g.setColor(new Color(0, 0, 0, 30));
            g.fillRoundRect(barX, currentY, barMaxWidth, barHeight, 8, 8);

            // Bar foreground
            g.setColor(colors[i]);
            int barWidth = (int) (Math.min(values[i], 255) / 255.0 * barMaxWidth);
            g.fillRoundRect(barX, currentY, barWidth, barHeight, 8, 8);

            // Value
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(values[i]), barX + barMaxWidth + (int)(15 * sw), currentY + (int)(18 * sh));

            currentY += spacing;
        }
    }

    private void drawAbilities(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        int padding = (int)(40 * sw);
        int currentY = y + (int)(70 * sh);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(32 * sw)));
        String title = "TALENTS";
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, x + (w - titleW) / 2, currentY);
        currentY += (int)(60 * sh);

        if (pkmn.getPrimaryAbility() != null) {
            currentY = drawAbilityCentered(g, pkmn.getPrimaryAbility(), language, x, currentY, w, sw, sh, false);
            currentY += (int)(20 * sh);
        }

        if (pkmn.getSecondaryAbility() != null) {
            currentY = drawAbilityCentered(g, pkmn.getSecondaryAbility(), language, x, currentY, w, sw, sh, false);
            currentY += (int)(20 * sh);
        }

        if (pkmn.getHiddenAbility() != null) {
            drawAbilityCentered(g, pkmn.getHiddenAbility(), language, x, currentY, w, sw, sh, true);
        }
    }

    private int drawAbilityCentered(Graphics2D g, Ability ability, Language language, int x, int y, int w, double sw, double sh, boolean hidden) {
        String name = getAbilityName(ability, language);
        if (hidden) name += " (Caché)";
        
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(24 * sw)));
        g.setColor(new Color(0, 80, 160));
        int nameW = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (w - nameW) / 2, y);

        int nextY = y + (int)(30 * sh);
        String desc = getAbilityDescription(ability, language);
        if (!desc.isEmpty() && !hidden) {
            g.setFont(new Font("SansSerif", Font.PLAIN, (int)(18 * sw)));
            g.setColor(Color.DARK_GRAY);
            nextY = drawWrappedTextCentered(g, desc, x, nextY, w, (int)(40 * sw), (int)(24 * sh));
        }
        return nextY;
    }

    private int drawWrappedTextCentered(Graphics2D g, String text, int x, int y, int w, int padding, int lineHeight) {
        if (text == null || text.isBlank()) return y;
        FontMetrics fm = g.getFontMetrics();
        int maxWidth = w - 2 * padding;
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(currentLine.toString() + word) < maxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        lines.add(currentLine.toString().trim());

        int currentY = y;
        for (String line : lines) {
            int lineW = fm.stringWidth(line);
            g.drawString(line, x + (w - lineW) / 2, currentY);
            currentY += lineHeight;
        }
        return currentY;
    }

    private void drawSprite(Graphics2D g, String url, int x, int y, int w, int h) {
        if (url == null || url.isBlank()) return;
        try {
            BufferedImage sprite = ImageIO.read(URI.create(url).toURL());
            if (sprite != null) {
                int sw = sprite.getWidth();
                int sh = sprite.getHeight();
                double ratio = Math.min((double) w / sw, (double) h / sh);
                int newW = (int) (sw * ratio);
                int newH = (int) (sh * ratio);
                int newX = x + (w - newW) / 2;
                int newY = y + (h - newH) / 2;
                g.drawImage(sprite, newX, newY, newW, newH, null);
            }
        } catch (Exception e) {
        }
    }

    private Color parseColor(String hex, Color fallback) {
        if (hex == null || hex.isEmpty()) return fallback;
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String getPkmnName(Pkmn pkmn, Language language) {
        return pkmn.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(PkmnTranslation::getPkmnName)
                .orElse(pkmn.getSymbol() != null ? pkmn.getSymbol() : "Unknown");
    }

    private String getFormName(Pkmn pkmn, Language language) {
        return pkmn.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(PkmnTranslation::getFormName)
                .orElse("normal");
    }

    private String getTypeName(Type type, Language language) {
        if (type == null) return "";
        return type.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(TypeTranslation::getName)
                .orElse(type.getSymbol() != null ? type.getSymbol() : "???");
    }

    private String getAbilityName(Ability ability, Language language) {
        if (ability == null) return "None";
        return ability.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(AbilityTranslation::getName)
                .orElse(ability.getSymbol() != null ? ability.getSymbol() : "???");
    }

    private String getAbilityDescription(Ability ability, Language language) {
        if (ability == null) return "";
        return ability.getLang().stream()
                .filter(l -> l.getLanguage() == language)
                .findFirst()
                .map(AbilityTranslation::getDescription)
                .orElse("");
    }
}