package org.antredesloutres.papi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antredesloutres.papi.model.domain.Ability;
import org.antredesloutres.papi.model.domain.Move;
import org.antredesloutres.papi.model.domain.Pkmn;
import org.antredesloutres.papi.model.enumerated.Language;
import org.antredesloutres.papi.model.translation.AbilityTranslation;
import org.antredesloutres.papi.model.translation.MoveTranslation;
import org.antredesloutres.papi.model.translation.PkmnTranslation;
import org.antredesloutres.papi.repository.AbilityRepository;
import org.antredesloutres.papi.repository.MoveRepository;
import org.antredesloutres.papi.repository.PkmnRepository;
import org.antredesloutres.papi.repository.TypeRepository;
import org.antredesloutres.papi.model.domain.Type;
import org.antredesloutres.papi.model.enumerated.EggGroup;
import org.antredesloutres.papi.model.enumerated.ExperienceGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatapackService {

    private final ObjectMapper objectMapper;
    private final PkmnRepository pkmnRepository;
    private final AbilityRepository abilityRepository;
    private final MoveRepository moveRepository;
    private final TypeRepository typeRepository;

    @Transactional
    public void importDatapack(MultipartFile file, String tag) {
        log.info("Starting datapack import with tag {}", tag);

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("datapack_");
            extractZip(file.getInputStream(), tempDir);

            // Pass 1: Parse entities
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    String fileName = path.toString().replace('\\', '/');
                    if (fileName.contains("data/cobblemon/abilities/") && fileName.endsWith(".js")) {
                        processAbility(extractSymbolFromPath(fileName), tag);
                    } else if (fileName.contains("data/cobblemon/moves/") && fileName.endsWith(".js")) {
                        processMove(extractSymbolFromPath(fileName), tag);
                    } else if ((fileName.contains("data/cobblemon/species/") || fileName.contains("data/cobblemon/species_additions/")) && fileName.endsWith(".json")) {
                        processSpeciesJson(path, tag);
                    }
                });
            }

            // Pass 2: Parse translations
            try (Stream<Path> paths = Files.walk(tempDir)) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    String fileName = path.toString().replace('\\', '/');
                    if (fileName.contains("/lang/") && fileName.endsWith(".json")) {
                        processLangFile(path, tag);
                    }
                });
            }

            // Pass 3: Post-process forms to inherit names and set form names
            List<Pkmn> allPkmn = pkmnRepository.findAll();
            for (Pkmn p : allPkmn) {
                if (p.getSymbol() != null && p.getSymbol().contains("-")) {
                    String[] parts = p.getSymbol().split("-", 2);
                    String baseSymbol = parts[0];
                    String formName = parts[1];
                    
                    Optional<Pkmn> basePkmnOpt = pkmnRepository.findBySymbol(baseSymbol);
                    
                    for (Language l : Language.values()) {
                        PkmnTranslation formTrans = p.getLang().stream()
                                .filter(t -> t.getLanguage() == l)
                                .findFirst()
                                .orElseGet(() -> {
                                    PkmnTranslation t = new PkmnTranslation();
                                    t.setLanguage(l);
                                    p.getLang().add(t);
                                    return t;
                                });
                                
                        if (basePkmnOpt.isPresent()) {
                            PkmnTranslation baseTrans = basePkmnOpt.get().getLang().stream()
                                    .filter(t -> t.getLanguage() == l)
                                    .findFirst()
                                    .orElse(null);
                            
                            if (baseTrans != null && (formTrans.getPkmnName() == null || formTrans.getPkmnName().isEmpty() || formTrans.getPkmnName().equalsIgnoreCase(formName))) {
                                formTrans.setPkmnName(baseTrans.getPkmnName());
                            }
                        }
                        
                        if (formTrans.getPkmnName() == null || formTrans.getPkmnName().isEmpty() || formTrans.getPkmnName().equalsIgnoreCase(formName)) {
                            String capitalizedBase = baseSymbol.substring(0, 1).toUpperCase() + baseSymbol.substring(1);
                            formTrans.setPkmnName(capitalizedBase);
                        }
                        
                        if (formTrans.getFormName() == null || formTrans.getFormName().equals("normal") || formTrans.getFormName().equalsIgnoreCase(baseSymbol)) {
                            formTrans.setFormName(formName.toUpperCase());
                        }
                    }
                    pkmnRepository.save(p);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process datapack zip", e);
            throw new RuntimeException("Failed to process datapack zip", e);
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    private void extractZip(InputStream is, Path targetDir) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path resolvedPath = targetDir.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(targetDir)) {
                    throw new RuntimeException("Entry is outside of the target dir: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(zis, resolvedPath);
                }
            }
        }
    }

    private void deleteDirectory(Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            log.warn("Could not delete file " + p);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to clean up temp dir", e);
        }
    }

    private String extractSymbolFromPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        if (lastSlash != -1 && lastDot != -1 && lastDot > lastSlash) {
            return path.substring(lastSlash + 1, lastDot);
        }
        return path;
    }

    private void processAbility(String symbol, String tag) {
        Ability ability = abilityRepository.findBySymbol(symbol).orElseGet(() -> new Ability(symbol));
        ability.getTags().add(tag);
        abilityRepository.save(ability);
    }

    private void processMove(String symbol, String tag) {
        Move move = moveRepository.findBySymbol(symbol).orElseGet(() -> {
            return new Move(symbol, symbol, null, 0, 100, 15);
        });
        move.getTags().add(tag);
        moveRepository.save(move);
    }

    private void processSpeciesJson(Path path, String tag) {
        try {
            JsonNode rootNode = objectMapper.readTree(path.toFile());
            String target = rootNode.has("target") ? rootNode.get("target").asText() : null;
            String symbol = null;
            
            if (target != null && target.startsWith("cobblemon:")) {
                symbol = target.substring("cobblemon:".length());
            } else if (rootNode.has("name")) {
                symbol = rootNode.get("name").asText();
            } else if (rootNode.has("target") && !rootNode.get("target").asText().contains(":")) {
                symbol = rootNode.get("target").asText();
            }

            if (symbol == null) {
                log.warn("Could not determine species symbol from JSON");
                return;
            }
            final String finalSymbol = symbol;

            if (rootNode.has("forms") && rootNode.get("forms").isArray() && rootNode.get("forms").size() > 0) {
                for (JsonNode form : rootNode.get("forms")) {
                    String formName = form.has("name") ? form.get("name").asText().toLowerCase() : "";
                    
                    if (formName.isEmpty() || formName.equals("normal")) {
                        Pkmn pkmn = pkmnRepository.findBySymbol(finalSymbol).orElseGet(() -> {
                            Pkmn p = new Pkmn();
                            p.setSymbol(finalSymbol);
                            return p;
                        });
                        pkmn.getTags().add(tag);
                        parseSpeciesFields(rootNode, pkmn, tag);
                        parseSpeciesFields(form, pkmn, tag);
                        pkmnRepository.save(pkmn);
                    } else {
                        String formSymbol = finalSymbol + "-" + formName;
                        Pkmn formPkmn = pkmnRepository.findBySymbol(formSymbol).orElseGet(() -> {
                            Pkmn p = new Pkmn();
                            p.setSymbol(formSymbol);
                            return p;
                        });
                        formPkmn.getTags().add(tag);
                        parseSpeciesFields(rootNode, formPkmn, tag);
                        parseSpeciesFields(form, formPkmn, tag);
                        pkmnRepository.save(formPkmn);
                    }
                }
            } else {
                Pkmn pkmn = pkmnRepository.findBySymbol(finalSymbol).orElseGet(() -> {
                    Pkmn p = new Pkmn();
                    p.setSymbol(finalSymbol);
                    return p;
                });
                pkmn.getTags().add(tag);
                parseSpeciesFields(rootNode, pkmn, tag);
                pkmnRepository.save(pkmn);
            }
        } catch (Exception e) {
            log.error("Failed to parse species JSON {}", path, e);
        }
    }

    private void parseSpeciesFields(JsonNode node, Pkmn pkmn, String tag) {
        if (node.has("nationalPokedexNumber")) pkmn.setNationalDexNumber(node.get("nationalPokedexNumber").asInt());
        
        if (node.has("height")) pkmn.setHeight((int)(node.get("height").asDouble() * 10));
        if (node.has("weight")) pkmn.setWeight((int)(node.get("weight").asDouble() * 10));
        if (node.has("catchRate")) pkmn.setCatchRate(node.get("catchRate").asInt());
        if (node.has("maleRatio")) {
            double ratio = node.get("maleRatio").asDouble();
            pkmn.setMaleRatio(ratio < 0 ? -1 : (int)(ratio * 100));
        }
        if (node.has("baseExperienceYield")) pkmn.setExperienceYield(node.get("baseExperienceYield").asInt());
        if (node.has("experienceGroup")) {
            try {
                pkmn.setExperienceGroup(ExperienceGroup.valueOf(node.get("experienceGroup").asText().toUpperCase().replace(" ", "_")));
            } catch (Exception ignored) {}
        }
        if (node.has("eggCycles")) pkmn.setEggCycles(node.get("eggCycles").asInt());
        if (node.has("baseFriendship")) pkmn.setBaseFriendship(node.get("baseFriendship").asInt());
        
        if (node.has("eggGroups") && node.get("eggGroups").isArray()) {
            pkmn.getEggGroups().clear();
            for (JsonNode eg : node.get("eggGroups")) {
                try {
                    pkmn.getEggGroups().add(EggGroup.valueOf(eg.asText().toUpperCase().replace(" ", "_")));
                } catch (Exception ignored) {}
            }
        }
        
        if (node.has("primaryType")) {
            typeRepository.findBySymbol(node.get("primaryType").asText().toLowerCase())
                .ifPresent(pkmn::setPrimaryType);
        }
        if (node.has("secondaryType")) {
            typeRepository.findBySymbol(node.get("secondaryType").asText().toLowerCase())
                .ifPresent(pkmn::setSecondaryType);
        }
        
        if (node.has("baseStats")) {
            JsonNode stats = node.get("baseStats");
            if (stats.has("hp")) pkmn.setBaseHp(stats.get("hp").asInt());
            if (stats.has("attack")) pkmn.setBaseAttack(stats.get("attack").asInt());
            if (stats.has("defence")) pkmn.setBaseDefense(stats.get("defence").asInt());
            if (stats.has("special_attack")) pkmn.setBaseSpeAttack(stats.get("special_attack").asInt());
            if (stats.has("special_defence")) pkmn.setBaseSpeDefense(stats.get("special_defence").asInt());
            if (stats.has("speed")) pkmn.setBaseSpeed(stats.get("speed").asInt());
        }
        
        if (node.has("evYield")) {
            JsonNode evs = node.get("evYield");
            if (evs.has("hp")) pkmn.setEvHp(evs.get("hp").asInt());
            if (evs.has("attack")) pkmn.setEvAttack(evs.get("attack").asInt());
            if (evs.has("defence")) pkmn.setEvDefense(evs.get("defence").asInt());
            if (evs.has("special_attack")) pkmn.setEvSpeAttack(evs.get("special_attack").asInt());
            if (evs.has("special_defence")) pkmn.setEvSpeDefense(evs.get("special_defence").asInt());
            if (evs.has("speed")) pkmn.setEvSpeed(evs.get("speed").asInt());
        }
        
        if (node.has("abilities") && node.get("abilities").isArray()) {
            int normalCount = 0;
            for (JsonNode abilityNode : node.get("abilities")) {
                String abilityName = abilityNode.asText();
                boolean isHidden = abilityName.startsWith("h:");
                if (isHidden) abilityName = abilityName.substring(2);
                
                processAbility(abilityName, tag);
                Ability ability = abilityRepository.findBySymbol(abilityName).orElse(null);
                
                if (isHidden) {
                    pkmn.setHiddenAbility(ability);
                } else {
                    if (normalCount == 0) {
                        pkmn.setPrimaryAbility(ability);
                        normalCount++;
                    } else if (normalCount == 1) {
                        pkmn.setSecondaryAbility(ability);
                        normalCount++;
                    }
                }
            }
        }
    }

    private void processLangFile(Path path, String tag) {
        try {
            String fileName = path.getFileName().toString().toLowerCase();
            Language lang = Language.EN;
            
            // Check language based on file name or prefix
            if (fileName.startsWith("fr") || fileName.contains("fr_")) {
                lang = Language.FR;
            } else if (fileName.startsWith("en") || fileName.contains("en_")) {
                lang = Language.EN;
            }

            // Using TypeReference to parse simple key-value JSON strings.
            Map<String, String> translations = objectMapper.readValue(path.toFile(), new TypeReference<Map<String, String>>() {});
            
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.startsWith("cobblemon.ability.")) {
                    String sub = key.substring("cobblemon.ability.".length());
                    boolean isDesc = sub.endsWith(".desc");
                    String symbol = isDesc ? sub.substring(0, sub.length() - 5) : sub;
                    
                    final Language finalLang = lang;
                    Optional<Ability> opt = abilityRepository.findBySymbol(symbol);
                    if (opt.isPresent()) {
                        Ability ability = opt.get();
                        AbilityTranslation trans = ability.getLang().stream()
                                .filter(t -> t.getLanguage() == finalLang)
                                .findFirst()
                                .orElseGet(() -> {
                                    AbilityTranslation t = new AbilityTranslation();
                                    t.setLanguage(finalLang);
                                    ability.getLang().add(t);
                                    return t;
                                });
                        if (isDesc) trans.setDescription(value);
                        else trans.setName(value);
                        abilityRepository.save(ability);
                    }
                } else if (key.startsWith("cobblemon.move.")) {
                    String sub = key.substring("cobblemon.move.".length());
                    boolean isDesc = sub.endsWith(".desc");
                    String symbol = isDesc ? sub.substring(0, sub.length() - 5) : sub;
                    
                    final Language finalLang = lang;
                    Optional<Move> opt = moveRepository.findBySymbol(symbol);
                    if (opt.isPresent()) {
                        Move move = opt.get();
                        MoveTranslation trans = move.getLang().stream()
                                .filter(t -> t.getLanguage() == finalLang)
                                .findFirst()
                                .orElseGet(() -> {
                                    MoveTranslation t = new MoveTranslation();
                                    t.setLanguage(finalLang);
                                    move.getLang().add(t);
                                    return t;
                                });
                        if (isDesc) trans.setDescription(value);
                        else trans.setName(value);
                        moveRepository.save(move);
                    }
                } else if (key.startsWith("cobblemon.species.")) {
                    String sub = key.substring("cobblemon.species.".length());
                    boolean isDesc = sub.endsWith(".desc");
                    boolean isName = sub.endsWith(".name");
                    if (isDesc || isName) {
                        String symbol = isDesc ? sub.substring(0, sub.length() - 5) : sub.substring(0, sub.length() - 5);
                        
                        final Language finalLang = lang;
                        Optional<Pkmn> opt = pkmnRepository.findBySymbol(symbol);
                        if (opt.isPresent()) {
                            Pkmn pkmn = opt.get();
                            PkmnTranslation trans = pkmn.getLang().stream()
                                    .filter(t -> t.getLanguage() == finalLang)
                                    .findFirst()
                                    .orElseGet(() -> {
                                        PkmnTranslation t = new PkmnTranslation();
                                        t.setLanguage(finalLang);
                                        pkmn.getLang().add(t);
                                        return t;
                                    });
                            if (isDesc) trans.setDescription(value);
                            else trans.setPkmnName(value);
                            pkmnRepository.save(pkmn);
                        }
                    }
                } else if (key.startsWith("cobblemon.ui.pokedex.info.form.")) {
                    String symbol = key.substring("cobblemon.ui.pokedex.info.form.".length());
                    final Language finalLang = lang;
                    Optional<Pkmn> opt = pkmnRepository.findBySymbol(symbol);
                    if (opt.isEmpty() && symbol.endsWith("-megarlm")) {
                        opt = pkmnRepository.findBySymbol(symbol.replace("-megarlm", "-rlm-mega"));
                    }
                    if (opt.isPresent()) {
                        Pkmn pkmn = opt.get();
                        PkmnTranslation trans = pkmn.getLang().stream()
                                .filter(t -> t.getLanguage() == finalLang)
                                .findFirst()
                                .orElseGet(() -> {
                                    PkmnTranslation t = new PkmnTranslation();
                                    t.setLanguage(finalLang);
                                    pkmn.getLang().add(t);
                                    return t;
                                });
                        trans.setFormName(value);
                        pkmnRepository.save(pkmn);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse lang file {}", path, e);
        }
    }
}
