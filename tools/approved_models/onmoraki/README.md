> Integrado na 0.6.56 após autorização do usuário. Este documento registra a auditoria histórica; o estado atual está em `docs/persona-timing-0.6.56.md`. Clipes completos preservados, dano após o final.

# Onmoraki — auditoria de modelagem 01

**23/10/2026 · Primeira versão para revisão visual.** Modelo real editável, textura e três animações. Nenhum recurso do mod substituído; nenhum JAR gerado ou instalado.

## Aparência e referência

A referência principal é uma captura do compêndio de **Persona 3 Reload**, preservada em `references/reload-compendium.png`: [página de Onmoraki no BWIKI](https://wiki.biligame.com/persona/P3R/%E9%98%B4%E6%91%A9%E7%BD%97%E9%AC%BC). A captura é do jogo da ATLUS; o site apenas a hospeda. O recorte de inspeção está em `references/reload-detail.png`, e o comparativo com a geometria criada está em `draft/onmoraki-reference-01.png`.

Cinco características orientam este estudo:

1. Cabeça clara e muito maior que o tronco, com base larga e topo que converge para a crista.
2. Olhos fechados em três traços escuros, sem acrescentar íris ou pupila à pose da referência.
3. Crista verde afilada, curvada para trás, com faixas escuras longitudinais.
4. Corpo pequeno castanho, com áreas ocres no peito, ombros e pernas.
5. Asas curtas e angulosas, pernas finas dobradas e pés pontudos.

O volume foi adaptado à linguagem de cuboides do projeto. Frente, perfil e costas são apresentados com a mesma textura. A parte posterior e a anatomia interna da boca são interpretações das áreas não esclarecidas pela captura, e não reconstruções verificadas do asset original.

O material promocional de [Shin Megami Tensei V publicado pela Nintendo](https://www.nintendo.com/en-gb/Games/Nintendo-Switch-games/Shin-Megami-Tensei-V-1987419.html) foi localizado como referência secundária de identidade. É outro jogo: não serve como prova das animações de Reload nem substitui a captura principal.

## Padrão de construção

- 96 cuboides nativos, 20 ossos, Box UV e atlas de 1024 × 1024.
- Dimensões inteiras na grade de autoria uniforme 8×; orientação North / −Z; raiz na origem.
- Cabeça, mandíbula, pescoço, cinco segmentos da crista, asas, pernas e pés articulados.
- A forma vem dos sólidos; olhos e manchas usam pintura em pixels. Sem malha arredondada, fotografia projetada, blur ou partículas para ocultar a geometria.
- Modelo flutuante: pés suspensos; a origem não implica apoio no chão.

Aplicam-se as convenções de `velvet-eclipse/tools/izanagi/MODELING.md`, inspiradas no [manual do Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model) e no [guia do Blockbench](https://blockbench.net/wiki/guides/minecraft-style-guide/). Os recursos são próprios; não foi copiada uma malha do jogo ou do Cobblemon.

## Animações entregues

| Clipe | Duração | Proposta nesta revisão |
| --- | --- | --- |
| `animation.onmoraki.air_idle` | 3,6 s | Flutuação discreta, batidas pequenas das asas, balanço das pernas e atraso da crista. |
| `animation.onmoraki.physical_attack` | 2,4 s | Recuo de preparação, investida curta da cabeça e do tronco, recolhimento das asas e recuperação. Pose de contato proposta em 0,80 s. |
| `animation.onmoraki.magic_attack` | 2,8 s | Elevação, abertura das asas, mandíbula aberta para projetar a magia à frente e retorno. Pose de emissão proposta em 1,05 s. |

**Os três ciclos são propostas autorais para a auditoria. Ainda não foram confirmados os clipes específicos de Onmoraki em Reload.** Foi pesquisado material público e inspecionada uma amostragem da compilação [ReVersed JRPGs — ALL Skills Showcase & Comparison](https://www.youtube.com/watch?v=TZap-dV5Oyo), já disponível no workspace. As amostras consultadas não forneceram uma sequência de Onmoraki identificada com segurança. Esse vídeo alterna Portable e Reload; não usar os trechos de Portable como confirmação.

Os tempos de impacto são marcadores do estudo, não mudanças no combate. A futura integração deverá adaptar os clipes à timeline real do mod, sem antecipar o dano ou a fala. Os GIFs de ataque têm câmera fixa. O giro em 360° é **somente uma inspeção do modelo**, separado das ações.

## O que revisar agora

- Proporção da cabeça, largura da base e transição para a crista.
- Leitura dos três traços dos olhos, contorno da boca e projeção da mandíbula.
- Silhueta do tronco, comprimento das pernas e proporção das asas.
- Intensidade do idle e clareza das propostas física e mágica.

Fidelidade das costas, formato da boca aberta e coreografia original continuam pontos a confirmar. Aprovar este estudo visual não implica que a animação tenha sido medida no jogo. Não apresentar como uma reprodução quadro a quadro.

## Arquivos

- `draft/onmoraki.bbmodel`: projeto Blockbench com atlas e três animações incorporados.
- `draft/onmoraki.geo.json`, `onmoraki.png`, `onmoraki.animation.json`: recursos separados.
- `draft/onmoraki-animations-01.gif`: os três clipes em sequência.
- GIFs individuais de idle, físico, mágico e giro de inspeção.
- Imagens de quatro vistas, cabeça, comparativo e poses dos ataques.
- `index.html`: galeria para revisão; GIFs e vídeos são renders offline dos recursos entregues.

## Validação e limites

O verificador confere dimensões, Box UV, hierarquia, textura incorporada, correspondência com o `.bbmodel`, chaves finitas, retorno à pose de espera e 23 contatos estruturais em 291 poses. O carregador nativo GeckoLib leu 20 ossos, 96 cuboides e os três clipes. O enquadramento é conferido nos quadros renderizados. Relatórios em `draft/validation.json`, `native-validation.json`, `framing-validation.json` e `media-validation.json`.

Foram preservados por hash 4.889 arquivos de produção e o JAR instalado, contados juntos. As prévias **não são capturas do Minecraft**. Escala no mundo/menu, conversão de eixos, iluminação com shaders, sincronização com efeitos e comportamento multiplayer pertencem a uma integração posterior autorizada. A conversão de eixos/escala deve ocorrer uma única vez no exportador; não copiar o JSON de autoria diretamente para produção.

## Reprodução

Python com NumPy e Pillow:

```text
python build_model.py
python verify_model.py
python present_model.py
python package_model.py
```

`NativeModelCheck.java` usa Java 17 e as dependências existentes do projeto. Compilá-lo e executá-lo antes do empacotamento; não produz um JAR do mod. As fontes tipográficas das prévias são Segoe UI do Windows. O empacotador usa o FFmpeg já disponível no workspace para gerar MP4s dos mesmos GIFs.

A pesquisa, o modelo e os geradores deste estudo permanecem isolados em `modeling/onmoraki-study-01/`. Aguardam a revisão do usuário antes de qualquer integração.
