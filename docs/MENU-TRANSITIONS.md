# 0.6.42 — Abertura do TAB e prévia de All-Out

O seletor de **TAB → System → Animação All-Out** agora ocupa a coluna direita. A animação com a skin do jogador fica numa área própria à esquerda, com recorte e escala que mantêm o corpo inteiro visível nos cinco estilos. As regiões de clique acompanham as novas posições; setas, roda do mouse, Enter e Esc mantêm as ações existentes.

A abertura do TAB foi estudada no vídeo fornecido `Pause Menu - Persona 3 Reload.mp4`, entre 2,30 e 3,02 segundos. A sequência implementada dura 0,72 segundo: passagem ciano com água e bolhas, giro do retrato, entrada do painel diagonal e apresentação escalonada das opções. A pose, escala e enquadramento finais aprovados permanecem os mesmos. A skin real e o filtro azul continuam ativos. O modo de movimento reduzido pula a abertura; TAB/Esc continuam permitindo fechar durante a transição. A navegação aguarda a entrada das opções terminar para evitar seleção acidental.

Não há mudança de protocolo, regras de jogo ou do inspect de Personas. Contador, pausa e corrida da versão 0.6.41 permanecem. O estudo visual do Tártaro foi feito fora das fontes e não altera blocos, texturas, geração nem mundos nesta versão.

## Verificação

- `menuFixTest`: separação entre seletor e animação, regiões de clique, corpo dentro do quadro nos cinco estilos para skins padrão e slim, duração, estado final, movimento reduzido e transformações em 30/60/144 FPS.
- `reloadMenuTest`: regressão focada das telas, navegação, dados reais e vínculos dos recursos do menu.
- `MenuGpuProbe opening`: shader azul e malha de produção renderizados em contexto OpenGL oculto; 36 quadros compostos com o layout Java de produção para inspeção offline.
- Prévia do All-Out obtida da geometria exportada de `AllOutPlayerRenderer`, com a mesma área e transformação do seletor.

As imagens e o GIF são verificações offline. Não houve controle do computador nem execução de teste visual dentro do Minecraft. Compatibilidade visual com cada shader do usuário ainda depende da conferência no jogo.
