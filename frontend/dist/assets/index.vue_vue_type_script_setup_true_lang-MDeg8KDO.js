const __vite__mapDeps=(i,m=__vite__mapDeps,d=(m.f||(m.f=["assets/video-viewer-CxHiiO8D.js","assets/index-CdIjDISF.js","assets/arco-design-vue-wpZPvk8Z.js","assets/arco-design-vue-icon-S64ufJhU.js","assets/index-wkWtydi5.css","assets/file-CTjv8ooS.js","assets/file-BMhh5CmS.css","assets/useFolderLevelTree-BY3VF7JM.js","assets/index-DeLZq7_u.js","assets/index-CsrA5E6V.css","assets/index-l1mJVvo3.css","assets/index-kGALwHyS.css","assets/index-C7_jW4Bp.css","assets/audio-viewer-C7veOe4t.js","assets/audio-viewer-DGLnwZrE.css","assets/image-viewer-BdBKnbRI.js","assets/unknown-viewer-Bcl89xpM.js","assets/code-viewer-CDhJqGAj.js","assets/code-viewer-B1FkR1F6.css","assets/index-CFpRJApZ.css","assets/index-BZfi0XrG.css","assets/index-DyocfVfh.css","assets/ppt-viewer-Bb7MWRD_.js","assets/vue-office_pptx-Ba3sxtxb.js","assets/ppt-viewer-D4UVHYfO.css","assets/pdf-viewer-C6xVwEsY.js","assets/vue-office_pdf-DhQZnNrP.js","assets/pdf-viewer-C2qfg0-6.css","assets/excel-viewer-Bol-rRao.js","assets/vue-office_excel-BqdIRKNI.js","assets/vue-office_excel-BcyczgTn.css","assets/excel-viewer-B-lnAC5o.css","assets/word-viewer-SkUsSiD_.js","assets/vue-office_docx-CMVc1-Qo.js","assets/vue-office_docx-QBxTY_vh.css"])))=>i.map(i=>d[i]);
/* empty css              *//* empty css              *//* empty css              *//* empty css              */import{i as z}from"./file-CTjv8ooS.js";import{T as N,w as $,u as j,d as G}from"./useFolderLevelTree-BY3VF7JM.js";import{i as K,j as U,h as t,s as h}from"./index-CdIjDISF.js";import{ag as v,ai as X,c as P,aE as A,bv as n,d as R,o as f,a as O,ap as i,ao as a,aG as s,an as I,aq as C,aZ as k,a$ as Y,av as Z,am as D,bq as H,aX as F,aY as L,b as J,bg as Q,be as W,bc as ee,bb as oe,bd as te,ba as ne}from"./arco-design-vue-wpZPvk8Z.js";const S=K("viewFileStore",()=>{const p=v(!1),o=v(null),r=v(null),_=v("text"),u=v(!1),d=U();let m=new AbortController;const E=()=>{m.abort(),m=new AbortController};X(p,(e,T)=>{T&&!e&&E()});const b=async()=>{if(o.value&&(z(o.value)&&o.value.status!==N.OK&&A.warning("视频未转码，传输速度和质量会受影响，转码后食用更佳"),!["unknown","zip"].includes(o.value.mediaType))){u.value=!0;try{r.value=null;const e=await $(o.value.id,m,d.params.shareId);r.value=e}catch(e){e.message!=="canceled"&&A.error("获取文件信息失败")}finally{u.value=!1}}},l=async e=>{o.value=e,p.value=!0,await b()},w={video:{component:n(()=>t(()=>import("./video-viewer-CxHiiO8D.js"),__vite__mapDeps([0,1,2,3,4,5,6,7,8,9,10,11,12]))),needFullscreen:!1},audio:{component:n(()=>t(()=>import("./audio-viewer-C7veOe4t.js"),__vite__mapDeps([13,1,2,3,4,5,6,7,8,14,9,10,11,12]))),needFullscreen:!1},image:{component:n(()=>t(()=>import("./image-viewer-BdBKnbRI.js"),__vite__mapDeps([15,1,2,3,4,5,6,7,8,9,10,11,12]))),needFullscreen:!1},zip:{component:n(()=>t(()=>import("./unknown-viewer-Bcl89xpM.js"),__vite__mapDeps([16,1,2,3,4,5,6,7,8,9,11,10,12]))),needFullscreen:!0},text:{component:n(()=>t(()=>import("./code-viewer-CDhJqGAj.js").then(e=>e.w),__vite__mapDeps([17,2,7,8,1,3,4,18,9,19,20,12,21,11]))),needFullscreen:!0},ppt:{component:n(()=>t(()=>import("./ppt-viewer-Bb7MWRD_.js"),__vite__mapDeps([22,23,2,1,3,4,5,6,7,8,24,9,10,11,12]))),needFullscreen:!0},pptx:{component:n(()=>t(()=>import("./ppt-viewer-Bb7MWRD_.js"),__vite__mapDeps([22,23,2,1,3,4,5,6,7,8,24,9,10,11,12]))),needFullscreen:!0},pdf:{component:n(()=>t(()=>import("./pdf-viewer-C6xVwEsY.js"),__vite__mapDeps([25,26,2,1,3,4,5,6,7,8,27,9,10,11,12]))),needFullscreen:!0},xlsx:{component:n(()=>t(()=>import("./excel-viewer-Bol-rRao.js"),__vite__mapDeps([28,29,2,30,1,3,4,5,6,7,8,31,9,10,11,12]))),needFullscreen:!0},csv:{component:n(()=>t(()=>import("./code-viewer-CDhJqGAj.js").then(e=>e.w),__vite__mapDeps([17,2,7,8,1,3,4,18,9,19,20,12,21,11]))),needFullscreen:!0},doc:{component:n(()=>t(()=>import("./word-viewer-SkUsSiD_.js"),__vite__mapDeps([32,33,2,34,1,3,4,5,6,7,8,9,10,11,12]))),needFullscreen:!0},docx:{component:n(()=>t(()=>import("./word-viewer-SkUsSiD_.js"),__vite__mapDeps([32,33,2,34,1,3,4,5,6,7,8,9,10,11,12]))),needFullscreen:!0},code:{component:n(()=>t(()=>import("./code-viewer-CDhJqGAj.js").then(e=>e.w),__vite__mapDeps([17,2,7,8,1,3,4,18,9,19,20,12,21,11]))),needFullscreen:!0},md:{component:n(()=>t(()=>import("./code-viewer-CDhJqGAj.js").then(e=>e.w),__vite__mapDeps([17,2,7,8,1,3,4,18,9,19,20,12,21,11]))),needFullscreen:!0},unknown:{component:n(()=>t(()=>import("./unknown-viewer-Bcl89xpM.js"),__vite__mapDeps([16,1,2,3,4,5,6,7,8,9,11,10,12]))),needFullscreen:!0}},y=P(()=>{var e;return w[((e=o.value)==null?void 0:e.mediaType)??"unknown"]});return{isModalShow:p,file:o,spinning:u,openPreviewModal:l,data:r,getData:b,mediaType:_,refreshControl:E,currentItem:y}}),se={class:"min-h-[500px]"},re=R({__name:"display-comp",props:{comp:{}},setup(p){const{spinning:o,file:r}=h(S());return(_,u)=>{const d=Y;return f(),O("div",se,[i(d,{loading:s(o),tip:"加载中...",style:{height:"100%",width:"100%",position:"absolute",display:"flex","justify-content":"center"}},{default:a(()=>[s(r)?(f(),I(C(_.comp),{key:0})):k("",!0)]),_:1},8,["loading"])])}}}),me=R({__name:"index",setup(p){const{isModalShow:o,file:r,currentItem:_}=h(S()),{textColor:u,bgColor:d}=h(j()),m=P(()=>({color:u.value,backgroundColor:d.value})),E=["1. office下的部分内容渲染可能有偏差，可下载后在本地查看","2. @vue-office/excel不支持解析csv，所以csv放在了code中，用monaco预览","3. 视频等待转码完毕食用更佳"];return(b,l)=>{var V;const w=D("icon-backward"),y=ee,e=D("icon-question-circle"),T=ne,q=oe,M=te,B=H;return f(),I(B,{visible:s(o),"onUpdate:visible":l[1]||(l[1]=c=>Z(o)?o.value=c:null),footer:!1,closable:!1,fullscreen:(V=s(_))==null?void 0:V.needFullscreen,"body-style":{padding:0,height:"100%"},width:460,"unmount-on-close":""},{title:a(()=>{var c,x;return[i(y,{content:"返回"},{default:a(()=>[i(w,{class:"hover:text-h-blue scale-150 absolute top-4 left-6",onClick:l[0]||(l[0]=g=>o.value=!1)})]),_:1}),s(r)?(f(),I(C(s(G)((c=s(r))==null?void 0:c.name)),{key:0,class:"scale-[2] m-5"})):k("",!0),F(" "+L((x=s(r))==null?void 0:x.name)+"  ",1),i(M,null,{content:a(()=>[i(q,{style:J({boxShadow:"0 0 5px #165DFF",...s(m)})},{header:a(()=>l[2]||(l[2]=[F(" 提示 ")])),default:a(()=>[(f(),O(W,null,Q(E,g=>i(T,{key:g},{default:a(()=>[F(L(g),1)]),_:2},1024)),64))]),_:1},8,["style"])]),default:a(()=>[i(e,{class:"hover:text-h-blue cursor-pointer"})]),_:1})]}),default:a(()=>{var c;return[i(re,{comp:(c=s(_))==null?void 0:c.component},null,8,["comp"])]}),_:1},8,["visible","fullscreen"])}}});export{me as _,re as a,S as u};
