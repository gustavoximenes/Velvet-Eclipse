'use strict';
// Standalone visual audit. These fixtures never send messages or purchases to Minecraft.
const shops = [
  {id:'aohige', name:'Aohige Pharmacy', theme:'pharmacy'},
  {id:'police', name:'Tatsumi East Police', theme:'police'},
  {id:'mayoido', name:'Mayoido Antiques', theme:'mayoido'},
  {id:'blue', name:'Be Blue V', theme:'blue'},
  {id:'chagall', name:'Chagall Café', theme:'activity chagall'},
  {id:'mandragora', name:'Mandragora', theme:'activity mandragora'},
  {id:'arcade', name:'Game Parade', theme:'activity arcade'},
  {id:'escapade', name:'Club Escapade', theme:'activity escapade'},
  {id:'kenneth', name:'Kenneth Arnold', theme:'activity kenneth'}
];
const item=(name,price,detail,icon='HP',extra={})=>({name,price,detail,icon,...extra});
const medicine=[
  item('Medicine',120,'Recupera HP de um aliado.'),
  item('Antídoto',180,'Remove o envenenamento de um aliado.','REC'),
  item('Bebida energética',300,'Um suprimento de recuperação para a exploração.','REC'),
  item('Kit de primeiros socorros',650,'Cuide da party antes de retornar ao Tártaro.'),
  item('Pomada medicinal',400,'Um item de recuperação para levar na próxima expedição.'),
  item('Suplemento vital',840,'Um reforço para o seu estoque de suprimentos.','REC')
];
const categories=['Comprar armas','Comprar armaduras','Comprar calçados','Comprar acessórios','Vender itens'];
const equipment=[
  [item('Espada curta',900,'Uma lâmina leve e equilibrada.','ARMA',{stat:'Ataque',from:24,to:32,accuracy:95}),item('Espada de aço',1800,'Mais alcance e poder de ataque.','ARMA',{stat:'Ataque',from:24,to:40,accuracy:93}),item('Arco reforçado',1650,'Equipamento para ataques à distância.','ARMA',{stat:'Ataque',from:22,to:38,accuracy:94})],
  [item('Colete reforçado',700,'Proteção para a próxima exploração.','DEF',{stat:'Defesa',from:18,to:23}),item('Jaqueta resistente',1400,'Tecido resistente e proteção adicional.','DEF',{stat:'Defesa',from:18,to:30})],
  [item('Botas leves',600,'Calçados leves para se mover com segurança.','EVAS',{stat:'Evasão',from:12,to:16}),item('Botas de combate',1250,'Calçados para incursões prolongadas.','EVAS',{stat:'Evasão',from:12,to:21})],
  [item('Amuleto',400,'Um acessório para completar o equipamento.','ACESS',{stat:'Vigor',from:5,to:6}),item('Bracelete protetor',1000,'Um reforço defensivo.','ACESS',{stat:'Defesa',from:18,to:21})]
];
const antiqueServices=['Fundir equipamento','Trocar cartas','Trocar itens'];
const recipes=[
  [item('Lâmina encantada',0,'Ataque 42 · Acerto 94 · Uma base transformada por materiais.','ARMA',{needs:{'Base de arma':1,'Ônix':2}}),item('Bracelete místico',0,'Um acessório produzido com gemas.','ACESS',{needs:{'Ametista':3,'Turquesa':2}}),item('Armadura encantada',0,'Defesa 32 · Uma nova proteção para a party.','DEF',{needs:{'Base de armadura':1,'Ônix':2}})],
  [item('Carta Bufu',0,'Carta de habilidade · Gelo para um inimigo.','GELO',{needs:{'Ônix':1}}),item('Carta Dia',0,'Carta de habilidade · Recuperação de HP.','CURA',{needs:{'Ametista':2}}),item('Carta Zio',0,'Carta de habilidade · Eletricidade para um inimigo.','ELEC',{needs:{'Turquesa':2}})],
  [item('Gema de gelo',0,'Item de ataque elemental de gelo.','GELO',{needs:{'Ônix':2}}),item('Goma espiritual',0,'Suprimento de recuperação de SP.','SP',{needs:{'Ametista':2}})]
];
const party=[{name:'Yukari Takeba',initials:'YT',persona:'Io',level:22,skills:['Garu','Magaru','Media','Sukunda']},{name:'Junpei Iori',initials:'JI',persona:'Hermes',level:22,skills:['Agi','Maragi','Assault Dive','Rakukaja']},{name:'Akihiko Sanada',initials:'AS',persona:'Polydeuces',level:22,skills:['Zio','Mazio','Tarunda','Sukunda']}];
const recalledByMember=[
  [item('Dia',0,'Recupera HP de um aliado.','CURA',{sp:4}),item('Patra',0,'Remove confusão, medo e aflições compatíveis.','REC',{sp:3}),item('Garu',0,'Dano de vento a um inimigo.','VENTO',{sp:3}),item('Sukunda',0,'Reduz a precisão e a evasão de um inimigo.','SUP',{sp:8})],
  [item('Cleave',0,'Ataque físico contra um inimigo.','CORTE',{hp:7}),item('Re Patra',0,'Ajuda um aliado a se levantar.','REC',{sp:3}),item('Agi',0,'Dano de fogo a um inimigo.','FOGO',{sp:3}),item('Rakukaja',0,'Aumenta a defesa de um aliado.','SUP',{sp:8})],
  [item('Dia',0,'Recupera HP de um aliado.','CURA',{sp:4}),item('Sonic Punch',0,'Ataque físico contra um inimigo.','IMP',{hp:9}),item('Zio',0,'Dano elétrico a um inimigo.','ELEC',{sp:3}),item('Tarunda',0,'Reduz o ataque de um inimigo.','SUP',{sp:8})]
];
const activityData={
  chagall:{speaker:'Chagall Café',prompt:'O aroma do café preenche o ambiente. O que deseja fazer?',meta:'CAFÉ · ¥ 500',rows:[item('Tomar café',500,'Recupera 20 SP.'),item('Talvez outra hora',0,'Voltar ao shopping.','',{back:true})]},
  mandragora:{speaker:'Mandragora',prompt:'Uma sala de karaokê está disponível. Vai cantar um pouco?',meta:'KARAOKÊ · ¥ 800',rows:[item('Cantar',800,'Treinar coragem uma vez por dia.'),item('Deixar para depois',0,'Voltar ao shopping.','',{back:true})]},
  arcade:{speaker:'Game Parade',prompt:'As máquinas estão ligadas. Quer iniciar uma sessão de treinamento?',meta:'TREINAMENTO · ¥ 1.500',rows:[item('Começar treinamento',1500,'Treinar a experiência do jogador uma vez por dia.'),item('Só estou olhando',0,'Voltar ao shopping.','',{back:true})]},
  escapade:{speaker:'Adividente',prompt:'Posso ajudá-lo a se preparar para a próxima expedição. Deseja uma consulta?',meta:'CONSULTA · ¥ 2.000',rows:[item('Consultar a adividente',2000,'Recupera HP e SP para a próxima expedição.'),item('Agora não',0,'Voltar ao shopping.','',{back:true})]},
  kenneth:{speaker:'Kenneth Arnold',prompt:'Discos e capas preenchem a vitrine. Não há atendimento de compras disponível.',meta:'VITRINE',rows:[item('Observar a vitrine',0,'Você observa os discos em exposição.','',{inspect:true}),item('Voltar ao shopping',0,'Encerrar a visita.','',{back:true})]}
};
const stage=document.getElementById('stage'),selector=document.getElementById('shops');
shops.forEach((s,i)=>selector.add(new Option(s.name,i)));
let state,stack=[],pending=null,previousFocus=null;
function reset(){state={shop:0,view:'root',selected:0,category:0,member:0,balance:5000,inventory:{Medicine:2},materials:{'Base de arma':1,'Base de armadura':0,'Ônix':4,'Ametista':1,'Turquesa':0},learned:{},status:'',emptyParty:false};stack=[];pending=null;selector.value='0';document.getElementById('empty-party').checked=false;render(true)}
const money=n=>'¥ '+n.toLocaleString('pt-BR');
const esc=t=>String(t).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
function shop(){return shops[state.shop]}
function choice(name){return {name,price:0}}
function currentRows(){
  if(state.view==='closed')return [choice('Retornar à loja')];
  if(shop().id==='aohige')return medicine;
  if(shop().id==='police'){
    if(state.view==='root')return categories.map(choice);
    if(state.category===4)return Object.entries(state.inventory).filter(([,q])=>q>0).map(([name,q])=>item(name,Math.floor((medicine.find(i=>i.name===name)?.price??400)/2),'Vender uma unidade do inventário de demonstração.','ITEM',{owned:q,sell:true}));
    return equipment[state.category];
  }
  if(shop().id==='mayoido')return state.view==='root'?antiqueServices.map(choice):recipes[state.category];
  if(shop().id==='blue')return state.view==='skills'?recalledByMember[state.member]:state.emptyParty?[]:party;
  return activityData[shop().id].rows;
}
function rowMarkup(rows,withIcons=false){return rows.map((r,i)=>`<button type="button" class="row ${state.selected===i?'selected':''}" data-row="${i}" aria-pressed="${state.selected===i}" aria-label="${esc(r.name)}${r.price?', '+money(r.price):''}">${withIcons?`<span class="item-icon" aria-hidden="true">${r.icon||'•'}</span>`:''}<span class="row-main">${esc(r.name)}</span><span class="row-end">${r.hp?`${r.hp}% HP`:r.sp?`${r.sp} SP`:r.price?money(r.price):r.owned?`× ${r.owned}`:''}</span></button>`).join('')}
function wallet(){return `<aside class="wallet" aria-label="Saldo de demonstração"><div><span>¥</span><span>${state.balance.toLocaleString('pt-BR')}</span></div><div class="pay"><span>A pagar</span><b>0</b></div></aside>`}
function footer(){return `<footer class="footer"><button type="button" data-action="activate"><kbd>Enter</kbd> OK</button><button type="button" data-action="back"><kbd>Esc</kbd> Voltar</button></footer><div class="status" role="status" aria-live="polite">${esc(state.status)}</div>`}
function pillArt(){return `<div class="pill-layer" aria-hidden="true">${[[103,248,'#6936b1',-32],[615,147,'#d71822',35],[857,-34,'#009879',-28],[1139,386,'#5720ad',-68],[106,544,'#0cb995',33]].map(([x,y,c,r],i)=>`<i class="capsule" style="left:${x}px;top:${y}px;--pill:${c};--rot:${r}deg;animation-delay:-${i*.7}s"></i>`).join('')}<i class="tablet" style="left:352px;top:298px"></i><i class="tablet" style="left:841px;top:150px"></i><i class="tablet" style="left:1067px;top:497px;transform:rotate(35deg)"></i></div>`}
function pharmacy(){const r=currentRows()[state.selected];return `${wallet()}<div class="folded-ad" aria-hidden="true"><div class="ad-brand">AOH<span>IGE</span></div><div class="ad-small">PHARMACY</div><div class="sale">SALE</div></div>${pillArt()}<section class="pharmacy-list"><h1 class="list-title">Suprimentos para a sua próxima expedição!</h1>${rowMarkup(medicine,true)}</section><aside class="pharmacy-detail"><p>${esc(r.detail)}</p><div class="owned">Em estoque <strong>${String(state.inventory[r.name]||0).padStart(2,'0')}</strong></div></aside>`}
function tapes(){return `<div class="tapes" aria-hidden="true">${['one','two','three'].map(c=>`<div class="tape ${c}">${'KEEP OUT '.repeat(15)}</div>`).join('')}</div>`}
function police(){const rows=currentRows(),r=rows[state.selected];return `${tapes()}${wallet()}<div class="police-sign" aria-label="Tatsumi East Police"><b>TATSUMI</b><b>EAST</b></div>${state.view==='root'?`<div class="police-vertical" aria-hidden="true">POLICE<small>TATSUMI EAST POLICE STATION</small></div><div class="weapon-stamp" aria-hidden="true">⚔</div><section class="directory"><small>PRIVATE CATALOGUE</small><h1 class="display">DIRECTORY</h1>${rowMarkup(rows)}</section><p class="police-speech">Do que<br>você precisa?</p>`:`<section class="equipment-list"><h1 class="list-title">${categories[state.category]}</h1>${rows.length?rowMarkup(rows,true):'<p class="empty-party">Nenhum item disponível para vender.</p>'}</section><aside class="equipment-detail"><h3>${r?esc(r.name):'Inventário vazio'}</h3>${r?.stat?`<div class="stat-line"><span>${r.stat}</span><span>${r.from} <b>→ ${r.to}</b></span></div>${r.accuracy?`<div class="stat-line"><span>Acerto</span><b>${r.accuracy}</b></div>`:''}`:''}<p>${r?esc(r.detail):'Volte ao catálogo para comprar equipamentos.'}</p>${r?`<div class="stat-line"><span>${r.sell?'Você recebe':'Preço'}</span><b>${money(r.price)}</b></div>`:''}</aside>`}`}
const ornament=`<svg viewBox="0 0 240 270" fill="none" aria-hidden="true"><defs><linearGradient id="botanic" x2=".3" y2="1"><stop stop-color="#ddd273"/><stop offset="1" stop-color="#46fda4"/></linearGradient></defs><g stroke="url(#botanic)" stroke-width="3"><path d="M19 270C62 186 226 208 181 102C171 75 137 82 144 107C150 127 173 113 161 98M43 243C-24 206 5 151 41 160C64 166 63 194 43 193C26 192 27 175 39 176M86 224C129 153 66 162 72 129C77 108 97 115 95 130M118 270C143 214 210 244 214 198C218 160 169 167 186 194M143 198C135 155 229 178 220 128C215 99 193 104 195 119M166 164C208 62 175 73 172 45C168 13 211 13 210 32C209 48 189 48 191 34M194 77C179 57 217 71 228 53"/><path d="M94 205L84 172M176 124L203 141M162 78L145 52"/></g><g fill="#9be7a0">${[[48,212],[108,161],[220,107],[155,50]].map(([x,y])=>Array.from({length:7},(_,i)=>`<ellipse cx="${x}" cy="${y-8}" rx="3" ry="8" transform="rotate(${i*51.428} ${x} ${y})"/>`).join('')).join('')}</g></svg>`;
function antiqueArt(){return `<svg class="antique-scroll" viewBox="0 0 1280 720" fill="currentColor" aria-hidden="true"><path d="M445 694C218 592 345 382 408 391C442 396 430 462 402 447C314 542 401 648 469 618C370 547 397 454 443 450C486 446 484 522 455 507C451 575 511 654 704 686C595 705 520 702 445 694Z"/><path d="M453 618C407 524 389 292 498 174C660 2 892 52 1015 204C922 109 703 61 565 192C431 320 463 520 589 607Z"/><path d="M435 552C278 661 183 549 224 495C247 465 284 483 272 505C263 521 244 512 250 501C202 547 305 610 399 530Z"/><path d="M393 457C303 419 234 469 253 397C262 363 318 353 322 388C325 414 299 425 291 407C275 424 355 409 397 446Z"/><path d="M450 640C535 682 651 676 670 622C630 661 579 645 583 619C589 584 639 603 626 572C609 537 549 555 547 595C543 640 610 664 690 649C601 732 498 698 450 640Z"/><path d="M220 417C229 386 265 364 281 385C291 401 270 417 260 404C249 397 262 390 266 398C269 380 239 392 220 417Z"/><path d="M961 485L996 420L1119 444L1068 515Z"/></svg><aside class="antique-brand"><div class="seal">古美術</div><strong>MAYOIDO<br>ANTIQUES</strong><small>ANTIGUIDADES</small>${ornament}</aside>`}
function antique(){let rows=currentRows(),r=rows[state.selected];return `${antiqueArt()}${state.view==='root'?`<section class="antique-root"><h1 class="list-title">Bem-vindo! Fique à vontade.</h1>${rowMarkup(rows)}</section>`:`<section class="antique-trade"><h1 class="list-title">${antiqueServices[state.category]}</h1>${rowMarkup(rows,true)}</section><aside class="materials"><h3>Materiais necessários</h3><dl>${Object.entries(r.needs).map(([name,n])=>`<dt>${name}</dt><dd class="${(state.materials[name]||0)<n?'missing':''}">${state.materials[name]||0} / ${n}</dd>`).join('')}</dl></aside><p class="trade-detail">${r.detail}</p>`}`}
function blue(){let rows=currentRows(),member=party[state.view==='skills'?state.member:state.selected]||party[0];return state.view==='skills'?`<header class="skill-banner"><small>BE BLUE V / RELEMBRAR HABILIDADE</small><h1>${member.name}</h1><span>${member.persona}</span><span>Lv ${member.level}</span></header><section class="learned-skills"><h2 class="display">HABILIDADES ATUAIS</h2><div class="learned-grid">${[...member.skills,...(state.learned[member.name]||[]),...Array(8).fill('—')].slice(0,8).map(s=>`<span>${s}</span>`).join('')}</div></section><section class="recall-list"><h1 class="display">SELECT SKILL</h1>${rowMarkup(rows,true)}</section><p class="skill-note">${rows[state.selected].detail}</p>`:`<div class="select-title" aria-hidden="true">SELECT</div><section class="party-list"><h1 class="party-heading">Relembrar habilidade <span>Quem você vai escolher?</span></h1>${rows.length?rowMarkup(rows):'<p class="empty-party">Nenhum membro disponível.<br>Volte quando sua party estiver reunida.</p>'}</section>${rows.length?`<aside class="member-card"><span class="monogram" aria-hidden="true">${member.initials}</span><strong class="member-name">${member.name.replace(' ','<br>')}</strong><span class="persona-name">${member.persona} &nbsp; Lv ${member.level}</span></aside><p class="blue-detail">Escolha um membro para consultar as habilidades que ele pode relembrar.</p>`:''}`}
function dialogue(speaker,text){return `<div class="speaker">${speaker}</div><div class="dialogue"><p>${text}</p><span class="cursor" aria-hidden="true">▼</span></div>`}
function activity(){let d=activityData[shop().id];return `${wallet()}<header class="location"><span><small>PAULOWNIA MALL</small>${shop().name}</span></header><section class="choices">${rowMarkup(d.rows)}</section><div class="activity-meta">${d.meta}</div>${dialogue(d.speaker,esc(d.prompt))}`}
function closeView(){return `<header class="location"><span>${shop().name}</span></header><section class="choices">${rowMarkup(currentRows())}</section>${dialogue(shop().name,'Até a próxima! Você pode retornar à loja ou escolher outro estabelecimento na barra da prévia.')}`}
function render(animate=false){
  const rows=currentRows();state.selected=Math.max(0,Math.min(state.selected,rows.length-1));
  stage.className=shop().theme+(state.view==='skills'?' blue-skills':'')+(state.view==='closed'?' activity':'');
  let content=state.view==='closed'?closeView():({aohige:pharmacy,police,mayoido:antique,blue}[shop().id]||activity)();
  stage.innerHTML=`<div class="backdrop" style="background-image:url(assets/${shop().id}.jpg)"></div><div class="wash"></div><div class="ui-layer" ${animate?'':'style="animation:none"'}>${content}</div>${footer()}`;
  stage.querySelectorAll('[data-row]').forEach(b=>b.addEventListener('click',()=>{let i=+b.dataset.row;if(state.selected===i)activate();else{state.selected=i;state.status='';render()}}));
  stage.querySelector('[data-action=activate]').onclick=activate;
  stage.querySelector('[data-action=back]').onclick=back;
  if(pending)renderModal();
}
function go(view,category=0){stack.push({view:state.view,selected:state.selected,category:state.category,member:state.member});state.view=view;state.category=category;state.selected=0;state.status='';render(true)}
function back(){
  if(pending){pending=null;render();restoreFocus();return}
  if(stack.length){Object.assign(state,stack.pop());state.status='';render(true)}
  else if(state.view==='closed'){state.view='root';state.selected=0;state.status='';render(true)}
  else{state.view='closed';state.selected=0;state.status='';render(true)}
}
function activate(){
  if(pending)return;
  if(state.view==='closed'){back();return}
  let rows=currentRows(),r=rows[state.selected];if(!r)return;
  if(shop().id==='police'&&state.view==='root'){go('equipment',state.selected);return}
  if(shop().id==='mayoido'&&state.view==='root'){go('trade',state.selected);return}
  if(shop().id==='blue'&&state.view==='root'){const member=state.selected;go('skills');state.member=member;render(true);return}
  if(r.back){back();return}
  if(r.inspect){state.status='Você observa os discos em exposição. Não há transação disponível.';render();return}
  const kind=shop().id==='mayoido'?'trade':shop().id==='blue'?'recall':shop().id==='police'&&r.sell?'sell':shop().id==='aohige'||shop().id==='police'?'buy':'activity';
  pending={kind,row:r,quantity:1,member:state.member};previousFocus={row:state.selected};render();
  stage.querySelector('#cancel-action').focus();
}
function restoreFocus(){stage.querySelector(`[data-row="${previousFocus?.row??state.selected}"]`)?.focus()}
function modalProblem(){
  if(!pending)return '';const {kind,row,quantity,member}=pending;
  if(!Number.isInteger(quantity)||quantity<1||quantity>9)return 'Escolha uma quantidade entre 1 e 9.';
  if(kind==='buy'||kind==='activity'){if(row.price*quantity>state.balance)return 'Saldo insuficiente para esta seleção.'}
  if(kind==='sell'&&(state.inventory[row.name]||0)<quantity)return 'Não há unidades suficientes no inventário.';
  if(kind==='trade'&&Object.entries(row.needs).some(([n,q])=>(state.materials[n]||0)<q))return 'Você ainda não possui todos os materiais necessários.';
  if(kind==='recall'&&(party[member].skills.includes(row.name)||(state.learned[party[member].name]||[]).includes(row.name)))return 'Este membro já conhece essa habilidade.';
  return '';
}
function renderModal(){
  const {kind,row,quantity}=pending;
  const title={buy:'Confirmar compra?',sell:'Confirmar venda?',trade:'Confirmar troca?',recall:'Relembrar habilidade?',activity:'Confirmar atividade?'}[kind];
  const summary=kind==='trade'?Object.entries(row.needs).map(([n,q])=>`${q} × ${n}`).join(' + '):kind==='recall'?party[pending.member].name:row.detail;
  const shade=document.createElement('div');shade.className='modal-shade';shade.innerHTML=`<section class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="modal-title" aria-describedby="modal-summary"><h2 id="modal-title">${title}</h2><p id="modal-summary"><b>${esc(row.name)}</b><br>${esc(summary)}</p>${kind==='buy'||kind==='sell'?`<label class="quantity-line">Quantidade <input id="quantity" type="number" min="1" max="9" step="1" value="${quantity}" aria-label="Quantidade"></label>`:''}<div class="modal-total"><span>${kind==='sell'?'Você recebe':kind==='trade'?'Materiais':kind==='recall'?'Habilidade':'Total'}</span><strong id="total">${kind==='trade'?'Troca':kind==='recall'?row.name:money(row.price*quantity)}</strong></div><div id="modal-error" class="modal-error" role="status">${modalProblem()}</div><div class="modal-actions"><button id="cancel-action" type="button">Cancelar</button><button id="confirm-action" type="button" class="primary">Confirmar</button></div></section>`;
  stage.append(shade);
  stage.querySelector('#cancel-action').onclick=back;
  stage.querySelector('#confirm-action').onclick=commit;
  const qty=stage.querySelector('#quantity');if(qty)qty.oninput=()=>{pending.quantity=Number(qty.value);stage.querySelector('#total').textContent=Number.isFinite(pending.quantity)?money(row.price*pending.quantity):'—';stage.querySelector('#modal-error').textContent=modalProblem()};
}
function commit(){
  if(!pending)return;const error=modalProblem();if(error){stage.querySelector('#modal-error').textContent=error;return}
  // Consume exactly one pending operation, even if a queued click reaches this handler twice.
  const p=pending;pending=null;let result='';
  if(p.kind==='buy'){state.balance-=p.row.price*p.quantity;state.inventory[p.row.name]=(state.inventory[p.row.name]||0)+p.quantity;result=`${p.quantity} × ${p.row.name} adicionado ao inventário de demonstração.`}
  if(p.kind==='sell'){state.balance+=p.row.price*p.quantity;state.inventory[p.row.name]-=p.quantity;result=`Venda simulada: ${p.quantity} × ${p.row.name}.`}
  if(p.kind==='trade'){for(const[n,q]of Object.entries(p.row.needs))state.materials[n]-=q;state.inventory[p.row.name]=(state.inventory[p.row.name]||0)+1;result=`Troca simulada: ${p.row.name}.`}
  if(p.kind==='recall'){(state.learned[party[p.member].name]??=[]).push(p.row.name);result=`${party[p.member].name} relembrou ${p.row.name} na demonstração.`}
  if(p.kind==='activity'){state.balance-=p.row.price;result='Atividade simulada. Tempo e atributos do jogo não foram alterados.'}
  state.status=result;render();restoreFocus();
}
selector.onchange=()=>{state.shop=+selector.value;state.selected=0;state.view='root';state.category=0;state.status='';stack=[];pending=null;render(true)};
document.getElementById('reset').onclick=reset;
document.getElementById('empty-party').onchange=e=>{state.emptyParty=e.target.checked;if(shop().id==='blue'){state.view='root';state.selected=0;stack=[];pending=null;state.status='';render(true)}};
document.addEventListener('keydown',e=>{
  if(pending){
    if(e.key==='Escape'){e.preventDefault();back();return}
    if(e.key==='Tab'){const focusables=[...stage.querySelectorAll('.confirm-dialog input,.confirm-dialog button')];let i=focusables.indexOf(document.activeElement);e.preventDefault();focusables[(i+(e.shiftKey?-1:1)+focusables.length)%focusables.length].focus();return}
    if(e.key==='Enter'&&e.target.tagName==='INPUT'){e.preventDefault();commit()}
    return;
  }
  if(e.target.closest('.preview-tools')||e.target.tagName==='INPUT')return;
  if(e.key==='ArrowDown'||e.key==='ArrowUp'){e.preventDefault();const count=currentRows().length;if(count){state.selected=(state.selected+(e.key==='ArrowDown'?1:-1)+count)%count;state.status='';render()}}
  if(e.key==='Enter'){e.preventDefault();if(e.target.matches('button[data-action=back]'))back();else if(e.target.matches('button[data-row]')){state.selected=+e.target.dataset.row;activate()}else activate()}
  if(e.key==='Escape'){e.preventDefault();back()}
});
function resize(){let scale=window.innerWidth/1280;stage.style.transform=`scale(${scale})`;document.getElementById('viewport').style.height=`${720*scale}px`}
window.addEventListener('resize',resize);
// Read-only snapshot for offline QA; no production/world API exists in this prototype.
window.PaulowniaPreview={snapshot:()=>JSON.parse(JSON.stringify({...state,modal:pending?{kind:pending.kind,name:pending.row.name,quantity:pending.quantity}:null,historyDepth:stack.length}))};
reset();resize();
