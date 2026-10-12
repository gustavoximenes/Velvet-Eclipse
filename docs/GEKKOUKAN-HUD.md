# 0.6.50 — Gekkoukan e HUD de exploração

Implementação das duas auditorias aprovadas: campus contínuo, três pavimentos, cobertura, pátio e ginásio ligado por uma galeria. As árvores usam `cherry_log`, `cherry_leaves`, `oak_log` e `oak_leaves` do Minecraft, com folhas persistentes.

Os 22 ambientes e módulos incluem hall com loja/armários, escadas, duas salas de aula, biblioteca, sala dos professores, enfermaria, conselho, artes, costura/economia doméstica, música, laboratório, ciências, transmissão, impressão, secretaria, orientação, sanitários, depósito, vestiários, ginásio e cobertura. A circulação substitui as salas isoladas da planta anterior. Os interiores complementares continuam sendo adaptações das auditorias; não são medições extraídas do jogo original. Piscina, observatório e salas esportivas ainda reservadas nas auditorias não fazem parte desta versão.

As cadeiras e bancos da escola entram no mesmo sistema de assentos do dormitório e shopping. Portas das salas abrem e fecham com a colisão acompanhando a folha. Computadores mantêm o comportamento anterior. Os cinco materiais da escola aparecem na aba de blocos do criativo, com nomes em inglês e português.

## Correções de cenário

- A folha da porta da cobertura do dormitório agora corresponde ao vão, com folga pequena junto ao batente.
- Armários dos quartos ficam menores; seus puxadores acompanham o novo volume.
- Superfícies exatamente duplicadas são eliminadas na exportação dos novos fragmentos. Pisos sobrepostos da escola ficam em alturas distintas.
- Células vazias deixadas por portas abertas viram ar, e formas sem colisão deixam de exibir a seleção de um cubo inteiro.
- Doze células sem geometria da colisão artificial em torno da fonte são removidas.

Os defeitos das capturas foram tratados na geometria e nos caminhos de renderização/colisão. A causa exata de cada artefato de uma captura com shaders não pode ser confirmada pelo teste offline.

## HUD

HP/SP compactos, com números e sem retrato grande; relógio menor; aviso de Shuffle Time em uma linha, com tecla real e destaque de três segundos ao ganhar uma utilização. O minimapa da cidade mostra uma região local, direção do jogador e pavimento. `M` continua abrindo o mapa completo e a viagem rápida.

Os elementos de exploração usam a mesma escala e reservam o espaço da hotbar, armadura, fome e XP do Minecraft. Não aparecem sobre telas de loja/inventário ou combate. Os menus das lojas, Personas e combate permanecem com seu desenho anterior. `DARK HOUR` e `SHUFFLE TIME` são tratados como títulos completos. Inglês continua sendo o padrão; Português permanece em TAB → SYSTEM.

## Mundos existentes e multiplayer

A planta passa da revisão 3 para 4. A atualização conserva a planta e os estados de porta anteriores para remover somente os blocos gerados reconhecidos. Blocos diferentes colocados pelo jogador são preservados e podem impedir a colocação de partes novas. Um bloco colocado pelo jogador idêntico ao original na mesma posição não pode ser distinguido do original.

Uma transformação noturna antiga termina sua restauração antes da reconstrução. Os identificadores anteriores dos fragmentos permanecem válidos para o diário de restauração. A nova área noturna inclui ginásio e galerias; a estação vizinha fica fora desse recorte. Os inventários e dados de blocos continuam no diário persistente.

O canal da cidade passa ao protocolo **5**: cliente e servidor precisam desta versão para compartilhar os novos modelos.

## Reprodução e verificação

As fontes de autoria e as malhas aprovadas estão em `tools/gekkoukan`. Com Python, NumPy e Pillow, a sequência de exportação é:

```text
python tools/gekkoukan/export.py
python tools/city_polish/furniture.py
python tools/gekkoukan/furniture.py
python tools/gekkoukan/materials_maps.py
python tools/gekkoukan/audit_paths.py
```

`audit_paths.py` verifica passagens com largura de 0,6 e altura de 1,8 bloco sobre as colisões exportadas, em vez de considerar somente as caixas gerais das salas. Também percorre os seis trajetos de subida e descida entre os três pavimentos e a cobertura, com degrau máximo de 0,6 bloco. `audit_legacy.py <jar-anterior>` compara todos os identificadores e vértices antigos, a planta e os estados anteriores das portas. As verificações Java relevantes são `gekkoukanHudTest`, `cityFunctionTest`, `cityPolishTest` e `localizationTest`.

As prévias são produzidas a partir das malhas e texturas dos recursos finais, em um renderizador offline. Não houve execução do Minecraft nem validação em shader packs nesta entrega, conforme solicitado.

Resultados finais: 22 ambientes alcançáveis e seis percursos completos de escada; 13.645 amostras de circulação no dormitório/shopping e 19 destinos; 4.810 verificações de móveis e retornos; 32.772.929 verificações de geometria e restauração; 79 verificações de HUD/migração e 2.649 de localização. Os 66.919 identificadores de geometria anteriores e seus 220.995.528 bytes de vértices foram comparados com o JAR 0.6.49 e permanecem idênticos.
