# Tártaro 0.6.43

O hall, o relógio, os andares, os portais e as escadarias usam modelos derivados das prévias aprovadas v2/v3. As malhas são compiladas em seções nativas de chunks, com texturas do atlas do Minecraft, iluminação de blocos e colisões próprias. Não há uma segunda sala sobreposta ao cenário antigo.

## Aparência e funcionamento

- Hall de entrada com piso azul e marfim, colunas caneladas, relógio em relevo, escadaria e portal verde no vão sob o relógio.
- Salas alongadas, salas com pilares, plataformas laterais e corredores em L, T e cruz. A escolha da decoração usa um hash separado: mantém a sequência aleatória do grafo, as conexões, as entradas, as escadas e os pontos de encontro/tesouro existentes.
- As salas com duas escadas mantêm as plataformas originais. Só aparecem em ligações retas N/S ou E/W, com uma sala conectada em cada ponta; o conjunto inteiro gira 90° quando necessário. Curvas, bifurcações e pontas usam outras variantes. As entradas têm uma faixa de cinco blocos reservada depois da colocação da decoração.
- A parede atrás da saída do hall está fechada. O túnel do relógio tem teto e laterais contínuos; os portais de retorno têm fechamento de alvenaria entre o recorte da parede e a moldura. A textura animada usa coordenadas do portal inteiro e não reinicia na divisão de chunks.
- Andares 10 e 20 com preparação, arena, bloqueio do guardião e escada. O bloqueio visual desaparece junto da barreira ao vencer o guardião. Andar 25 com três baús e serviços, preservando as regras de Monad. Outros intervalos reutilizam os modelos correspondentes.
- Escadas com degraus de meio bloco, patamar e portal no destino real. Porta dupla da Velvet Room com acabamento nas duas faces, abertura e passagem usando o serviço original. Terminal e baús mantêm os mesmos IDs e interações.
- Novos blocos na aba criativa de blocos: ladrilho, alvenaria, vitral, luz, escadas e dez peças ornamentais (colunas, lanterna, corrimão, cornija, selo, arco, molduras e escadaria). As peças ornamentais são decoração; o bloco de escadas e os blocos sólidos servem à construção transitável. Terminal, baú e Monad continuam disponíveis como itens de colocação na categoria de objetos.
- O minimapa usa o contorno transitável da nova planta e exclui os pilares. Acrescenta retorno, terminal, Velvet, Monad, portal do relógio e bloqueio. Mantém descoberta por setor, compartilhamento da party e visibilidade dos tesouros. Protocolo `map_clock`: **2**, exigindo a mesma versão no cliente e servidor.

## Mundos existentes

A revisão arquitetônica é **2**. Ela também atualiza os andares já gerados com o JAR preliminar 0.6.43 usado no teste, sem depender da criação de outro mundo.

A versão da aparência é salva separadamente da geração/população. A atualização preserva semente, ciclo, inimigos derrotados, guardiões, tesouros recolhidos, terminais descobertos e UUIDs dos objetos. Um andar em combate espera o término da batalha. Jogadores em um andar que precisa da atualização são levados temporariamente à saída e retornam à entrada quando a construção termina, se permanecerem próximos do ponto de espera. Inimigos carregados ficam suspensos durante a troca e voltam a uma posição segura da sua sala. Uma construção interrompida é retomada por reconstrução idempotente na próxima solicitação.

## Shaders e limites da validação

Os detalhes verdes e as lanternas são superfícies emissivas separadas da alvenaria, com luz de bloco 15. O mapeamento de emissores `10028`, carregado pelo OptiFine a partir de `assets/velvet_eclipse/shaders/block.properties`, integra essas superfícies ao material emissivo do Complementary Reimagined 5.9.3 (bloom/reflexos conforme opções do shader). A alvenaria não recebe esse mapeamento. Incluem-se também mapas `_s` com emissão labPBR/SEUS e `_e` para texturas emissivas OptiFine. Nenhum ZIP ou configuração do shader instalado é alterado. Outros pacotes dependem dos padrões de material que suportam.

O cenário usa materiais opacos, normais, texturas e buffers nativos, sem aplicação global de brilho máximo à pedra. Vitrais/lanternas e luzes arquitetônicas fornecem iluminação; portais usam sprites animados. Os modelos são divididos nas mesmas seções 16×16×16 dos chunks para evitar desaparecimento de salas por distância ao bloco de origem. Os caches têm limites de memória.

A geometria e as proporções podem ser reproduzidas com os modelos personalizados. Cor final, exposição, bloom e sombras variam conforme shader e configuração; não se promete igualdade de pixels em todos os pacotes.

Foram usados testes offline e renderização OpenGL em contexto oculto. Nenhum controle de computador, inicialização do Minecraft ou teste visual com shaders dentro do jogo foi realizado. As imagens de auditoria são identificadas como offline; a iluminação nelas é uma aproximação, não uma captura do jogo.

## Reprodução da auditoria

Com JDK 17: `gradlew.bat tartarusReworkTest tartarusMapTest --offline --console=plain`. O primeiro verifica 54 andares comuns em seis sementes, hall, guardiões, transições, migração e materiais; o segundo verifica descoberta, população e protocolo do minimapa.

`python tools/tartarus_rework/audit_assets.py` confere recursos, hashes das malhas, modelos de itens, traduções e portais animados. Pillow é necessário para essa auditoria de imagens.

`python tools/tartarus_rework/audit_repairs.py` verifica a cobertura das paredes/tetos/portais, as duas orientações das plataformas originais, seus acessos livres e os mapas emissivos. O teste Java também percorre rotas com colisão real em dois blocos de altura, exige salas vizinhas nas duas pontas das plataformas e verifica a atualização do salvamento preliminar. O baker Forge verifica que somente superfícies luminosas entram no material emissivo e que a textura do relógio é contínua na emenda entre chunks.

`TartarusNativeModelAudit` executa o baker Forge real e exporta os vértices resultantes em `build/reports/tartarus-rework/native`. Essa checagem opcional usa os JARs/nativos LWJGL 3.3.1 locais no classpath, sem abrir janela. `prepare_native_audit.py` e `RenderNative.java` permitem inspecionar esses vértices e os materiais em OpenGL oculto.

As malhas entregues já estão nos recursos. Para regenerar, use Python com NumPy/Pillow, execute `export_assets.py` e depois `block_assets.py` em `tools/tartarus_rework`. Os planos originais usados como referência estão em `tools/tartarus_rework/plans`.
