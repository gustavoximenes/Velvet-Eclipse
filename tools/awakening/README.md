# Coreografia do despertar

Execute `python export_scene.py` nesta pasta (Python 3, NumPy e Pillow) para reconstruir `src/main/resources/assets/velvet_eclipse/awakening/*.bin.gz` a partir dos modelos e animações do projeto.

Os arquivos contêm malhas de cuboides, UVs, duas variantes de braços do jogador, câmeras e matrizes de ossos. A textura do jogador é obtida do cliente em tempo real. Não há textura de jogador nem vídeo incorporado.

Formato binário big endian: magic VEAW, versão, malhas (textura UTF-8, variante 0/1/2, vértices x/y/z/u/v, índice de camada), início em segundos, FPS, número de amostras, e para cada amostra câmera de 14 floats seguida de 16 floats por malha. Uma matriz nula oculta a malha. O gzip tem timestamp zero para exportação reproduzível.

A introdução cobre 0–16 s. A escolha interativa espera a confirmação. As continuações começam em 23 s e incluem o tempo das falas originais. Veja `../../docs/AWAKENING.md`.
