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

        // 1. Name Bar (Middle Top)
        int nameBarY = (int)(100 * sh);
        int nameBarH = (int)(115 * sh);
        drawName(g, pkmn, language, (int)(785 * sw), nameBarY, (int)(630 * sw), nameBarH, sw, sh);

        // 2. Type Bar (Middle Bottom)
        int typeBarY = (int)(865 * sh);
        int typeBarH = (int)(115 * sh);
        drawTypes(g, pkmn, language, (int)(785 * sw), typeBarY, (int)(630 * sw), typeBarH, sw, sh);

        // 3. Sprite Area (Between bars)
        int spriteY = nameBarY + nameBarH;
        int spriteH = typeBarY - spriteY;
        drawSprite(g, pkmn.getSpriteUrl(), (int)(850 * sw), spriteY, (int)(500 * sw), spriteH);

        // 4. Stats Box (Top Left)
        drawStats(g, pkmn, (int)(100 * sw), (int)(100 * sh), (int)(610 * sw), (int)(420 * sh), sw, sh);

        // 5. Abilities Box (Bottom Left)
        drawAbilities(g, pkmn, language, (int)(100 * sw), (int)(560 * sh), (int)(610 * sw), (int)(420 * sh), sw, sh);

        // 6. Info Box (Top Right)
        drawInfo(g, pkmn, language, (int)(1480 * sw), (int)(100 * sh), (int)(360 * sw), (int)(520 * sh), sw, sh);

        // 7. National Dex Circle (Bottom Right)
        drawDexNumber(g, pkmn.getNationalDexNumber(), (int)(1540 * sw), (int)(660 * sh), (int)(300 * sw), (int)(300 * sh), sw, sh);

        g.dispose();
        return image;
    }

    private void drawName(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        String name = getPkmnName(pkmn, language).toUpperCase();
        String form = getFormName(pkmn, language);
        
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(64 * sw)));
        FontMetrics fmN = g.getFontMetrics();
        
        if (form != null && !form.equalsIgnoreCase("normal")) {
            g.setFont(new Font("SansSerif", Font.ITALIC, (int)(32 * sw)));
            FontMetrics fmF = g.getFontMetrics();
            int gap = (int)(8 * sh);
            int totalH = fmN.getAscent() + gap + fmF.getAscent();
            int startY = y + (h - totalH) / 2 + fmN.getAscent();
            
            g.setFont(new Font("SansSerif", Font.BOLD, (int)(64 * sw)));
            g.drawString(name, x + (w - fmN.stringWidth(name)) / 2, startY);
            g.setFont(new Font("SansSerif", Font.ITALIC, (int)(32 * sw)));
            g.drawString("(" + form + ")", x + (w - fmF.stringWidth("(" + form + ")")) / 2, startY + gap + fmF.getAscent());
        } else {
            int textY = y + (h / 2) + ((fmN.getAscent() - fmN.getDescent()) / 2);
            g.drawString(name, x + (w - fmN.stringWidth(name)) / 2, textY);
        }
    }

    private void drawTypes(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        List<Type> list = new ArrayList<>();
        list.add(pkmn.getPrimaryType());
        if (pkmn.getSecondaryType() != null) list.add(pkmn.getSecondaryType());

        int tW = (int)(200 * sw);
        int tH = (int)(75 * sh);
        int gap = (int)(30 * sw);
        int totalW = list.size() * tW + (list.size() - 1) * gap;
        int startX = x + (w - totalW) / 2;
        int typeY = y + (h - tH) / 2;

        for (int i = 0; i < list.size(); i++) {
            Type t = list.get(i);
            if (t == null) continue;
            g.setColor(parseColor(t.getColor(), new Color(168, 168, 120)));
            g.fillRoundRect(startX + i * (tW + gap), typeY, tW, tH, 30, 30);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, (int)(32 * sw)));
            String name = getTypeName(t, language).toUpperCase();
            FontMetrics fm = g.getFontMetrics();
            int tx = startX + i * (tW + gap) + (tW - fm.stringWidth(name)) / 2;
            int ty = typeY + (tH / 2) + ((fm.getAscent() - fm.getDescent()) / 2);
            g.drawString(name, tx, ty);
        }
    }

    private void drawInfo(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        String title = "INFORMATIONS";
        String[][] items = {
            {"Taille", String.format("%.1f m", pkmn.getHeight() / 100.0)},
            {"Poids", String.format("%.1f kg", pkmn.getWeight() / 10.0)},
            {"Exp Base", String.valueOf(pkmn.getExperienceYield())},
            {"Capture", String.valueOf(pkmn.getCatchRate())}
        };

        g.setFont(new Font("SansSerif", Font.BOLD, (int)(36 * sw)));
        int titleH = g.getFontMetrics().getAscent();
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(28 * sw)));
        int rowH = (int)(80 * sh);
        int totalH = titleH + (int)(40 * sh) + (items.length * rowH);
        
        int currentY = y + (h - totalH) / 2 + titleH;
        int paddingX = (int)(60 * sw);

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(36 * sw)));
        g.drawString(title, x + (w - g.getFontMetrics().stringWidth(title)) / 2, currentY);
        currentY += (int)(60 * sh);

        for (String[] item : items) {
            g.setFont(new Font("SansSerif", Font.BOLD, (int)(28 * sw)));
            g.drawString(item[0], x + paddingX, currentY);
            g.setFont(new Font("SansSerif", Font.PLAIN, (int)(28 * sw)));
            int valW = g.getFontMetrics().stringWidth(item[1]);
            g.drawString(item[1], x + w - paddingX - valW, currentY);
            currentY += rowH;
        }
    }

    private void drawStats(Graphics2D g, Pkmn pkmn, int x, int y, int w, int h, double sw, double sh) {
        String[] labels = {"PV", "ATK", "DEF", "SPA", "SPD", "VIT"};
        int[] values = { pkmn.getBaseHp(), pkmn.getBaseAttack(), pkmn.getBaseDefense(), pkmn.getBaseSpeAttack(), pkmn.getBaseSpeDefense(), pkmn.getBaseSpeed() };
        Color[] colors = { new Color(255, 100, 100), new Color(255, 175, 120), new Color(255, 230, 120), new Color(130, 180, 255), new Color(160, 230, 130), new Color(255, 140, 180) };

        int padY = (int)(80 * sh);
        int rowH = (h - 2 * padY) / 6;
        int startY = y + padY;
        int padX = (int)(60 * sw);
        int labelW = (int)(110 * sw);
        int barH = (int)(32 * sh);
        int barMaxW = w - 2 * padX - labelW - (int)(70 * sw);

        g.setFont(new Font("SansSerif", Font.BOLD, (int)(28 * sw)));
        FontMetrics fm = g.getFontMetrics();
        int bY = (rowH / 2) + ((fm.getAscent() - fm.getDescent()) / 2);
        int vY = (rowH - barH) / 2;

        for (int i = 0; i < 6; i++) {
            int rY = startY + i * rowH;
            g.setColor(Color.BLACK);
            g.drawString(labels[i], x + padX, rY + bY);
            g.setColor(new Color(0, 0, 0, 30));
            g.fillRoundRect(x + padX + labelW, rY + vY, barMaxW, barH, 15, 15);
            g.setColor(colors[i]);
            int bW = (int)(Math.min(values[i], 255) / 255.0 * barMaxW);
            g.fillRoundRect(x + padX + labelW, rY + vY, bW, barH, 15, 15);
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(values[i]), x + padX + labelW + barMaxW + (int)(15 * sw), rY + bY);
        }
    }

    private void drawAbilities(Graphics2D g, Pkmn pkmn, Language language, int x, int y, int w, int h, double sw, double sh) {
        List<Ability> list = new ArrayList<>();
        if (pkmn.getPrimaryAbility() != null) list.add(pkmn.getPrimaryAbility());
        if (pkmn.getSecondaryAbility() != null) list.add(pkmn.getSecondaryAbility());
        
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(42 * sw)));
        int tH = g.getFontMetrics().getAscent();
        int totalH = tH + (list.size() * (int)(120 * sh));
        if (pkmn.getHiddenAbility() != null) totalH += (int)(60 * sh);
        
        int currentY = y + (h - totalH) / 2 + tH;
        g.setColor(Color.BLACK);
        String title = "TALENTS";
        g.drawString(title, x + (w - g.getFontMetrics().stringWidth(title)) / 2, currentY);
        currentY += (int)(80 * sh);

        for (Ability a : list) {
            currentY = drawAbilityCentered(g, a, language, x, currentY, w, sw, sh, false);
            currentY += (int)(40 * sh);
        }
        if (pkmn.getHiddenAbility() != null) {
            drawAbilityCentered(g, pkmn.getHiddenAbility(), language, x, currentY, w, sw, sh, true);
        }
    }

    private int drawAbilityCentered(Graphics2D g, Ability ability, Language language, int x, int y, int w, double sw, double sh, boolean hidden) {
        String name = getAbilityName(ability, language) + (hidden ? " (Caché)" : "");
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(32 * sw)));
        g.setColor(new Color(0, 100, 200));
        g.drawString(name, x + (w - g.getFontMetrics().stringWidth(name)) / 2, y);
        int nextY = y + (int)(40 * sh);
        String desc = getAbilityDescription(ability, language);
        if (!desc.isEmpty() && !hidden) {
            g.setFont(new Font("SansSerif", Font.PLAIN, (int)(24 * sw)));
            g.setColor(Color.DARK_GRAY);
            nextY = drawWrappedTextCentered(g, desc, x, nextY, w, (int)(60 * sw), (int)(32 * sh));
        }
        return nextY;
    }

    private int drawWrappedTextCentered(Graphics2D g, String text, int x, int y, int w, int pad, int lH) {
        if (text == null || text.isBlank()) return y;
        FontMetrics fm = g.getFontMetrics();
        int maxW = w - 2 * pad;
        String[] words = text.split(" ");
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(cur.toString() + word) < maxW) cur.append(word).append(" ");
            else { lines.add(cur.toString().trim()); cur = new StringBuilder(word + " "); }
        }
        lines.add(cur.toString().trim());
        int cY = y;
        for (String line : lines) {
            g.drawString(line, x + (w - fm.stringWidth(line)) / 2, cY);
            cY += lH;
        }
        return cY;
    }

    private void drawSprite(Graphics2D g, String url, int x, int y, int w, int h) {
        if (url == null || url.isBlank()) return;
        try {
            BufferedImage s = ImageIO.read(URI.create(url).toURL());
            if (s != null) {
                double r = Math.min((double) w / s.getWidth(), (double) h / s.getHeight());
                int nW = (int) (s.getWidth() * r);
                int nH = (int) (s.getHeight() * r);
                g.drawImage(s, x + (w - nW) / 2, y + (h - nH) / 2, nW, nH, null);
            }
        } catch (Exception e) {}
    }

    private void drawDexNumber(Graphics2D g, Integer d, int x, int y, int w, int h, double sw, double sh) {
        if (d == null) return;
        String t = String.format("#%04d", d);
        g.setFont(new Font("SansSerif", Font.BOLD, (int)(76 * sw)));
        g.setColor(new Color(30, 30, 30));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(t, x + (w - fm.stringWidth(t)) / 2, y + (h / 2) + ((fm.getAscent() - fm.getDescent()) / 2));
    }

    private Color parseColor(String h, Color f) { if (h == null || h.isEmpty()) return f; try { return Color.decode(h); } catch (Exception e) { return f; } }
    private String getPkmnName(Pkmn p, Language l) { return p.getLang().stream().filter(t -> t.getLanguage() == l).findFirst().map(PkmnTranslation::getPkmnName).orElse(p.getSymbol() != null ? p.getSymbol() : "Unknown"); }
    private String getFormName(Pkmn p, Language l) { return p.getLang().stream().filter(t -> t.getLanguage() == l).findFirst().map(PkmnTranslation::getFormName).orElse("normal"); }
    private String getTypeName(Type t, Language l) { if (t == null) return ""; return t.getLang().stream().filter(tr -> tr.getLanguage() == l).findFirst().map(TypeTranslation::getName).orElse(t.getSymbol() != null ? t.getSymbol() : "???"); }
    private String getAbilityName(Ability a, Language l) { if (a == null) return "None"; return a.getLang().stream().filter(tr -> tr.getLanguage() == l).findFirst().map(AbilityTranslation::getName).orElse(a.getSymbol() != null ? a.getSymbol() : "???"); }
    private String getAbilityDescription(Ability a, Language l) { if (a == null) return ""; return a.getLang().stream().filter(tr -> tr.getLanguage() == l).findFirst().map(AbilityTranslation::getDescription).orElse(""); }
}