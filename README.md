# Demon Slayer Mod (Fabric 1.21.1)

Ambiente de desenvolvimento pronto para um mod de Demon Slayer (Kimetsu no Yaiba).

## Requisitos

- **JDK 21** (Temurin/Adoptium recomendado). Confira com `java -version`.
  As versões do projeto (Loom 1.17, Gradle 9.5.1) foram escolhidas de propósito para rodar em Java 21. Não atualize o Loom para 1.19+ sem trocar para JDK 25.
- **IntelliJ IDEA** (Community serve) ou **VS Code** com o Java Extension Pack.
- Internet na primeira execução (o Gradle baixa o Minecraft, mappings e Fabric API).

## Como começar

1. Extraia o projeto e abra a pasta no IntelliJ (`File > Open` e escolha a pasta, importando como projeto Gradle).
2. Espere o Gradle sincronizar (a primeira vez demora vários minutos).
3. Rode o jogo:
   - IntelliJ: execute a configuração `Minecraft Client` que o Loom gera, ou
   - Terminal: `./gradlew runClient` (Windows: `gradlew.bat runClient`)
4. Para gerar o `.jar` do mod: `./gradlew build` (saída em `build/libs/`).

Outras tarefas úteis: `./gradlew runServer`, `./gradlew genSources` (gera fontes do Minecraft para você consultar no IDE).

## Estrutura

```
src/main/java/com/demonslayer/
  DemonSlayerMod.java        # entrypoint principal (servidor + cliente)
  item/ModItems.java         # registro de itens
  item/ModItemGroups.java    # aba do modo criativo
  item/ModToolTiers.java     # material da lâmina Nichirin
src/client/java/.../DemonSlayerClient.java   # entrypoint só do cliente (renderização, teclas)
src/main/resources/
  fabric.mod.json            # metadados do mod
  assets/demonslayer/        # texturas, modelos, traduções (en_us, pt_br)
  data/demonslayer/recipe/   # receitas
```

## Mappings

O projeto usa os **mappings oficiais da Mojang** (`net.minecraft.world.item.Item`, `ResourceLocation`, etc.), não Yarn.
Cuidado ao copiar tutoriais: muitos usam nomes do Yarn (`Identifier`, `ItemGroup`...) e precisam ser traduzidos.

## O que já vem pronto

- Item `nichirin_ingot` e `nichirin_katana` (espada com tier próprio), com receitas, modelos e traduções EN/PT-BR.
- Aba "Demon Slayer" no criativo.
- Texturas são **placeholders** de 16x16: substitua em `assets/demonslayer/textures/item/`.

## Próximos passos sugeridos

1. **Respirações** (Água, Chama, Trovão...): crie um item/habilidade que dispare efeitos via `use()` no item ou keybind no cliente (`KeyBindingHelper`).
2. **Demônios**: entidade customizada com `FabricDefaultAttributeRegistry`, renderer no cliente e spawn em biomas/noite.
3. **Sol e Nichirin**: dano extra a demônios usando uma tag de entidades (`data/demonslayer/tags/entity_type/demons.json`) e checando no `hurt`.
4. **Sangue Demoníaco (BDA)**: efeitos de status customizados (`MobEffect`).
5. **Minério e geração de mundo**: placed/configured features em `data/`.
6. **Data generation** (`fabric-datagen`) para parar de escrever JSON na mão quando o mod crescer.

## Solução de problemas

- `Unsupported class file major version`: o Gradle está usando JDK errado, configure JDK 21.
- Itens aparecem com textura roxa/preta: confira o caminho `assets/demonslayer/textures/item/<nome>.png` e o modelo em `models/item/`.
- Nome em tradução aparece como `item.demonslayer.xxx`: falta a chave no `lang/*.json`.
