# Nekomata revisão 02 — integrada na 0.6.52

Modelo aprovado pelo usuário, substituindo o antigo no ID `nekomata`. O registro completo da revisão visual foi preservado em [STUDY.md](STUDY.md); as indicações de integração pendente nesse documento descrevem a etapa anterior à aprovação.

- [Modelo editável](draft/nekomata.bbmodel), geometria, PNG e animações de autoria em `draft/`.
- [Galeria da revisão](index.html), imagens e GIFs renderizados dos recursos de autoria.
- [Parâmetros e hashes de integração](integration.json).
- [Entrega e verificações do mod](../../../docs/NEKOMATA-CITY.md).

266 cuboides, 47 ossos, Box UV, atlas 1024 × 1024 e grade 8×. O exportador aplica a conversão de eixos, renomeia os clipes para `misc.idle`, `action.physical` e `action.magic`, e alinha os impactos ao combate. A escala 0,125 é aplicada pelo renderer. Os JSONs de autoria não devem ser copiados diretamente para os recursos.

Na raiz do projeto:

```text
python tools/approved_models/import_nekomata.py
gradlew.bat --offline --no-daemon approvedModelsTest personaFramingExport --args=nekomata
gradlew.bat --offline --no-daemon personaFramingTest
python tools/approved_models/preview_nekomata.py
```

Para reconstruir a autoria, execute `python build_model.py` nesta pasta, com NumPy/Pillow. O contorno facial calculado já está em `facial_volume.json`; `fit_face.py` é opcional e requer SciPy. As pranchas comparativas prontas estão incluídas; sua reconstrução depende dos estudos anteriores e quadros de referência no workspace original. Os verificadores de integração reproduzíveis estão em `src/test/java/.../ApprovedModelsAudit.java` e `PersonaFramingAudit.java`, e os relatórios em `draft/` são o histórico da auditoria independente.

Os ataques físico e mágico foram reconstruídos de trechos de Reload observados no vídeo TZap-dV5Oyo. O período completo do idle não foi confirmado; sua cadência de 3,2 s continua aproximada. Validação offline, sem execução do Minecraft.
