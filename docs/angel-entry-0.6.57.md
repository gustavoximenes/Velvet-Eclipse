# Velvet Eclipse 0.6.57 — Angel, entradas e correção do Photon

01/11/2026. Integração autorizada após a aprovação da Angel revisão 03 e da corrida revisão 2.

## Conteúdo

- Angel 03 substitui os recursos do ID `angel`: 357 cuboides, 79 bones, atlas 1024, Box UV e escala uniforme de 0,125. Mantidos os encaixes revisados de pernas, joelhos, tornozelos, braços e corrente. Idle de 4,8 s, ataque físico de 2,6 s e mágico de 3,0 s, com expressões faciais nos clipes. As quatro expressões avulsas contêm apenas os canais do rosto, sem substituir a pose corporal. A venda permanece opaca, sem olhos inventados.
- Limites do menu recalculados com o modelo e os clipes reais do GeckoLib; carta de fusão e ícone criativo renderizados da geometria integrada. Catálogo, nível, afinidades, habilidades e ID do item de colocação preservados.
- Entrada neutra e com vantagem usam a corrida aprovada, sem inclinação lateral de tela: passada articulada, tronco 12° para a frente e dolly em perspectiva. Mantidos o rig standard/slim, a arma equipada e, na arena, a skin real do participante. A máscara angular revela a arena existente.
- Desvantagem abre diretamente para a arena com impacto e cor vermelha, sem a corrida azul. Vantagem e desvantagem têm aviso no piso em inglês/português; neutro não recebe aviso genérico BATTLE.
- Servidor transmite a variante e o tempo decorrido apenas aos participantes da sessão. Formação curta e duração visual são separadas: comandos, ataques inimigos e ações ficam bloqueados até 74 ticks (neutro/vantagem) ou 62 ticks (desvantagem). A entrada é cancelada ao encerrar a sessão, morrer ou trocar de mundo. Pacotes repetidos/antigos não reiniciam a animação.
- Photon: substituído o Cone de raio zero, que gerava direção/posição NaN, por Dot. Textura branca das malhas registrada no atlas de blocos; OBJ usa UV local. Exporter preserva a correção.

## Preservado

Fogo da evocação, voz inglesa no disparo da Evoker, mixer por categorias, dano/efeitos após o clipe completo da Persona, cidade, móveis, lojas, dificuldade e os demais modelos aprovados. Nenhuma edição de mundo ou colisão foi introduzida pela transição de entrada.

## Validação e limites

Verificações offline com GeckoLib e Photon reais: modelo/transformações, enquadramento em 181 Personas, duração e reserva de ataques, feedback, partículas, entradas standard/slim e famílias de armas, codecs, isolamento e cancelamento de sessões, proporções 4:3/16:9/21:9 e GUI menor, multiplayer e câmera/visibilidade. As prévias mostram a geometria de produção renderizada offline. Não houve execução do Minecraft ou teste visual OpenGL.

Angel conserva a adaptação aprovada: o giro observado na referência não identifica por si só o tipo do ataque; o ataque físico é uma proposta autoral. As durações das entradas também são adaptações ao mod, não extrações de clipes proprietários.

Cliente e servidor devem usar esta versão. Protocolos: principal 24, exploração **5**, cidade 9, menu 7.

## Fontes e reprodução

- `tools/approved_models/angel/`: fonte editável, documentação, atlas, modelo Blockbench e validações da revisão 03.
- `python tools/approved_models/import_angel.py`: conversão única de eixos para os recursos do GeckoLib, sem retiming ou conversão repetida de escala.
- `gradlew personaFramingExport --args=angel`: limites animados reais para o menu.
- `gradlew approvedModelsTest personaFramingTest`: validação nativa e camadas do menu.
- `python tools/approved_models/preview_angel.py`: carta, ícone e prévias nativas.
- `python tools/approved_models/build_entry_banners.py`: avisos de piso originais/localizados.
- `gradlew entryReleaseTest multiplayerTest battleCameraVisibilityTest`: pose, apresentação, rede e isolamento.
- `gradlew personaTimingTest elementalConfigTest elementalLifecycleTest feedbackTest`: resultado após o clipe e correção Photon.

Referências inspecionadas nas auditorias: [Angel no compêndio](https://www.youtube.com/watch?v=s-Ya33_6Wfs&t=230s), [entradas neutra e com vantagem](https://www.youtube.com/watch?v=5-OXq5NfnKA&t=91s), [Shadow Advantage](https://www.youtube.com/watch?v=23lwR5yn4Gw&t=30s). Nenhum vídeo externo foi incluído no JAR.
