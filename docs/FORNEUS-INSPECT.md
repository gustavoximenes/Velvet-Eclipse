# Forneus — inspect 0.6.47

O modelo horizontal estava sendo observado de baixo. A projeção de inventário inverte Y; a inclinação positiva anterior mostrava o ventre. A câmera agora observa o plano de natação por cima, mantendo a direção de três quartos e a coroa na posição superior.

A alteração é restrita aos previews de menu que usam `ApprovedPersonaFraming`. Recalculamos somente as 72 envoltórias projetadas do idle do Forneus. Permanecem iguais a largura de referência de 466 unidades no canvas de 900 × 500, o rig, as texturas, os ataques e as escalas de combate. Os dados das outras 180 Personas são idênticos aos da versão anterior.

Validação offline: `personaFramingExport`, `personaFramingTest`, `cityIntegrationTest` e empacotamento com `jar sourcesJar`. Verificadas 24 direções de câmera acima do plano do Forneus, 104.256 limites de viewport e 2.453.610 verificações da integração existente. A prévia usa geometria nativa GeckoLib exportada e as camadas reais do menu. Não houve teste no Minecraft ou com shader packs.

A auditoria de aparência do dormitório é um artefato separado: nenhuma geometria da proposta, mudança de planta ou migração da cidade foi incluída neste JAR. A aprovação visual será feita local por local.

A tentativa adicional de `build` encontrou uma falha preexistente em `BestiaryAudit`: a asserção exige exatamente dez abas, mas o menu já contém a aba Party. O teste e `MenuDesign` são idênticos aos da 0.6.46; nenhum usa a projeção modificada neste ponto. Essa suíte geral não passou e não foi alterada neste ajuste. O empacotamento é feito após os testes focados acima.
