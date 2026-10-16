# Cidade — correções 0.6.53

Revisão dos quartos e áreas próximas do dormitório, da Gekkoukan e da proteção dos cenários. A versão mantém as Personas aprovadas na 0.6.52.

## Dormitório

- Seis quartos: armários e puxadores reposicionados junto às paredes laterais, com espaço livre nas entradas.
- Espelhos, pias, geladeiras e luminárias alinhados às paredes. Arandelas da escada orientadas para a parede correta.
- Escrivaninhas, livros, luminárias de mesa e cadeiras movidos juntos para as janelas; pontos de sentar atualizados.
- Cortinas alinhadas às janelas reais e sustentadas por varões. Camas, sofás, banco e encostos recebem apoios.
- Estantes das salas de leitura aproximadas da parede. Computadores e interfaces das lojas preservados.

## Gekkoukan

- Dois pilares contínuos sustentam os patamares; vigas transversais e longarinas sob os seis lances eliminam o aspecto de escada flutuante. Corrimãos acompanham os lances.
- Vidros dos estudos de interiores passam a usar o material de vidro; as molduras de portas deixam de compartilhar faces visíveis com a parede.
- Luminárias encostadas no teto, iluminação dos corredores e do hall, apoios de encostos, macas e trilho da cortina da enfermaria.
- Armários de parede e quadros ajustados. Fachadas, ligações cobertas e pátio continuam no mesmo campus conectado; árvores continuam sendo blocos do Minecraft.

## Renderização, interação e atualização

O bloco de arquitetura usa forma dinâmica por posição, evitando o cache de uma forma vazia obtida sem sua entidade. A textura da folha acompanha a rotação da porta. A emissão luminosa dos componentes estáticos é preservada nas variantes e sincronizada no estado do bloco ao mudar a porta.

A cidade usa a revisão 6 e o protocolo de rede 7. Os identificadores, vértices, planta e estados de portas da revisão 5 ficam preservados para a migração dos mundos instalados. O diário da Dark Hour anterior é restaurado antes da reconstrução. Blocos diferentes colocados anteriormente pelo jogador continuam sendo tratados como conflitos, sem exclusão indiscriminada.

Na cidade inteira e na Velvet Room, eventos do servidor impedem quebrar/colocar blocos, uso de baldes, transformação por ferramentas, fogo por itens, alterações por explosões, pistões, abertura de portais comuns e destruição por criaturas. A regra também vale no criativo. Portas, assentos, lojas e portais do mod continuam utilizáveis; o construtor e a transformação da Dark Hour usam suas operações próprias. Outras dimensões não recebem esta proteção.

Nenhum bloco ou item novo é criado nesta correção; as abas criativas existentes são mantidas.

## Validação

Testes offline conferem geometria renderizável, UVs, emissão luminosa, estados de portas, assentos, migração 3/4/5 → 6, circulação, fechamento da escola e encaixes de móveis/escadas. As prévias usam a geometria exportada do mod e iluminação aproximada. Não são capturas do Minecraft e não certificam a aparência sob um shader específico.

A auditoria/modelagem dos vendedores do Paulownia Mall será a etapa seguinte, usando Elizabeth como referência de construção de personagens.
