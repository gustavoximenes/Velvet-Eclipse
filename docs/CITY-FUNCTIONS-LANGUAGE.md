# 0.6.49 — Cidade funcional, idiomas e nova torre

## Controles

- **TAB → SYSTEM → Idioma / Language**: Enter alterna English / Português. Inglês é o padrão, independentemente do idioma do Minecraft. A preferência é local, persistida em `config/velvet-eclipse-language.txt`; cada participante escolhe seu idioma.
- Clique direito nas cadeiras/bancos modelados para sentar; **SHIFT** para levantar. São 51 posições, incluindo os assentos do dormitório e do shopping.
- Clique direito nas folhas das portas para abrir/fechar. São 15 conjuntos; a colisão acompanha a folha, e o fechamento é recusado se atingir um jogador ou outra entidade viva.
- Os seis computadores conservam a geometria e o comportamento anterior.

## Retorno à cidade

As saídas da Velvet Room pelo shopping e do Tártaro agora carregam os chunks de destino antes de verificar o piso real. A busca considera a altura fracionária da arquitetura e o volume do jogador. Um destino temporariamente obstruído preserva a informação de retorno para nova tentativa; não envia o jogador incorretamente ao Overworld. O retorno ao amanhecer também permite novas tentativas.

## Tártaro aprovado e integrado

O usuário aprovou a auditoria exterior 01 e pediu substituir a torre antiga. Esta versão integra sua geometria: nove volumes deslocados, relógios com aros e ponteiros, faixas de escadas externas e acesso frontal. A superfície exportada preserva as dimensões e a área da proposta; as divisões por bloco não simplificam a silhueta. A ponta chega a 237,05 unidades e ocupa 238 camadas acima da origem. Na cidade, a origem é Y=64.

A torre usa 56.642 blocos locais, incluindo 42 células do portal. As 9.751 células luminosas emitem luz; as superfícies verdes têm material LabPBR emissivo. Geometria, profundidade e materiais passam pelos buffers normais dos chunks. O resultado de bloom/reflexos depende do shader pack; os testes desta entrega não executaram Minecraft nem shader packs.

A torre substitui a escola durante a Dark Hour, e o registro persistente restaura a escola, seus blocos e inventários ao amanhecer. Mundos salvos com a torre antiga restauram primeiro o registro anterior e constroem a nova. O exterior do Overworld também usa o novo modelo e preserva seu registro de terreno. A altura da fundação respeita o teto do mundo. Os andares internos, suas conexões e seu progresso são preservados. O minimapa noturno usa a nova planta do exterior.

O bloco **Tartarus Tower Masonry / Alvenaria da Torre do Tártaro** está na categoria criativa de blocos. Colocado individualmente, usa uma amostra sólida; a estrutura gerada configura os fragmentos nativos completos. Cliente e servidor devem atualizar juntos: protocolo de cidade **4**.

## Idiomas

Menus, HUDs, descrições de habilidades, equipamentos, lojas, cidade, combate, mensagens de interação e nomes registrados de blocos/itens passam pela seleção local. As mensagens do servidor são componentes com identificação de tradução; a mesma mensagem pode aparecer em inglês e português para dois clientes. IDs, comandos, dados de gameplay e nomes próprios de Personas/habilidades permanecem estáveis. A seleção não muda o idioma do Minecraft nem o texto de chat escrito pelos jogadores.

## Reprodução e verificações

Fontes da torre em `tools/tower_audit`; fonte dos móveis em `tools/city_polish/furniture.py`. Para regenerar: execute o exportador da cidade, o dos móveis e então `build.py OUTPUT` / `export.py OUTPUT` da torre. `validate_export.py OUTPUT NATIVE_OUTPUT` reconstrói a geometria entregue para comparação; `RenderTower` e `present.py --integrated` produzem as vistas offline. Python requer NumPy e Pillow; compilação Java usa o wrapper e as dependências incluídas.

Verificações focadas:

- `tartarusExteriorTest`: 340.426 verificações, portal, passagem do jogador, limites de construção, material emissivo, criativo e migração persistente.
- `cityPolishTest`: 22.935.714 verificações da geometria local, atlas, colisões, serviços e registro de restauração; 13 vistas de loja.
- `cityFunctionTest`: 2.131 verificações dos 51 assentos, 15 portas, pisos reais e destinos bloqueados.
- `localizationTest`: 2.649 verificações de persistência, idioma padrão, mensagens serializadas, IDs e desenho do menu SYSTEM nos dois idiomas.
- `cityIntegrationTest`, `velvetRoomFixTest` e `reloadMenuTest`: regressões focadas também executadas nesta etapa; detalhes nos registros de validação da entrega.
- Comparação geométrica: 894.094 triângulos após divisão em blocos; erro relativo de área menor que 4×10⁻¹¹, limites coincidentes com a fonte aprovada.

O empacotamento verifica todos os arquivos do JAR contra as classes e recursos produzidos. A instalação preserva o JAR anterior, confirma hashes e mantém os demais mods. A suíte global não foi repetida: existe uma asserção antiga e não relacionada em `BestiaryAudit` sobre a quantidade de abas. As verificações listadas são offline.
