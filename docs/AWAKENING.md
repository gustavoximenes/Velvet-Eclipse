# Despertar — 0.6.29

Jogadores novos começam sem Persona. Durante a Dark Hour, usar a Evoker com o botão direito inicia o despertar. A cena para no menu de escolha; A/D, setas ou mouse selecionam Orpheus, Izanagi ou Arsène. Enter ou o botão Confirmar sela a escolha. Ao terminar a sequência, o servidor entrega e equipa apenas a Persona escolhida, registra-a no Compendium e concede **I am thou Thou art I**.

Quem já possui Personas conserva o progresso. A Evoker não concede outra Persona a esses personagens. A capacidade inicial continua em dois espaços. Esc cancela a sequência sem recompensa; é possível tentar novamente. A Evoker não é consumida.

## Receita

Na bancada, use `IIL / ␠RI / ␠S␠`: I = lingote de ferro, L = lápis-lazúli, R = redstone, S = graveto. O resultado é a Evoker existente, também disponível na aba criativa de sua categoria.

## Apresentação

A cena usa geometria 3D e poses exportadas dos modelos aprovados. A skin atual do próprio jogador é aplicada na renderização, com braços clássicos ou slim e as opções de segunda camada. Nenhum avatar fixo, GIF ou vídeo substitui o jogador. As câmeras, o saque com braço articulado, a manifestação de corpo inteiro no feixe azul e os ataques finais seguem a prévia aprovada. O corpo da Persona permanece na mesma altura durante a materialização.

Os sete streams OGG são derivados dos áudios revisados: introdução, três loops musicais de escolha e três continuações com fala “Persona!”, contrato, música até o final. O volume respeita a categoria Música. A cena suprime outros sons durante a apresentação e encerra seus streams ao sair. Nenhuma gravação original completa foi incluída.

## Servidor e interrupções

Cada jogador recebe uma sessão e token independentes. O servidor exige o fim da introdução antes de aceitar uma escolha válida, rejeita a troca posterior e exige a duração da continuação antes da recompensa. Um pacote de conclusão isolado não concede uma Persona. A persistência e a conquista são gravadas antes de encerrar a sessão.

A cena é privada ao cliente. Não cria estruturas, Shadows reais ou Personas permanentes no mundo. O jogador fica imobilizado e protegido enquanto a sessão existe; encerramento da Dark Hour, morte, mudança de dimensão, desconexão, cancelamento e perda de comunicação removem essa proteção. Não modifica permanentemente modo de jogo, câmera, voo ou invulnerabilidade.

## Fontes e verificação

`tools/awakening/choreography.py` preserva a coreografia aprovada; `export_scene.py` exporta cuboides, UVs e matrizes dos ossos a 20 Hz, interpoladas no cliente. Requer Python, NumPy e Pillow apenas para regenerar as poses, não para compilar ou jogar. Os recursos gerados já acompanham o projeto.

Auditorias offline verificam criação e migração de dados, clonagem após morte, recompensas, escolha imutável, duração, formato dos recursos, finitude das matrizes e equivalência matemática da projeção 3D às câmeras aprovadas. As auditorias adjacentes de progressão, permissões, Personas iniciais e Shuffle Time também foram executadas. A validação visual direta dentro do Minecraft continua dispensada pelo usuário.
