import{H as I}from"./index-DeLZq7_u.js";import{i as T,g as r}from"./index-CdIjDISF.js";import{ap as a,ag as d,c,aE as g,aL as F}from"./arco-design-vue-wpZPvk8Z.js";const q=e=>({video:a("img",{src:"/assets/icon/wenjianleixing-biaozhuntu-shipinwenjian.png",width:"12"},null),audio:a("img",{src:"/assets/icon/yinlewenjian.png",width:"16"},null),image:a("img",{src:"/assets/icon/tupianwenjian.png",width:"16"},null),text:a("img",{src:"/assets/icon/text.png",width:"16"},null),ppt:a("img",{src:"/assets/icon/pptx.png",width:"16"},null),pptx:a("img",{src:"/assets/icon/pptx.png",width:"16"},null),pdf:a("img",{src:"/assets/icon/pdfwenjian.png",width:"16"},null),xlsx:a("img",{src:"/assets/icon/xlsx.png",width:"16"},null),csv:a("img",{src:"/assets/icon/csv.png",width:"16"},null),doc:a("img",{src:"/assets/icon/docx.png",width:"16"},null),docx:a("img",{src:"/assets/icon/docx.png",width:"16"},null),code:a("img",{src:"/assets/icon/a-appround6.png",width:"16"},null),md:a("img",{src:`/assets/icon/${e?"markdown-dark":"markdown"}.png`,width:"16"},null),zip:a("img",{src:"/assets/icon/yasuobao.png",width:"16"},null),folder:a("img",{src:"/assets/icon/wenjianjia.png",width:"16"},null),unknown:a("img",{src:`/assets/icon/${e?"file":"file-dark"}.png`,width:"16"},null)});function w(e){const s=e.substring(e.lastIndexOf(".")).toLowerCase();let n="unknown";switch(s){case".mp4":case".avi":case".mov":case".mkv":case".flv":n="video";break;case".mp3":case".wav":n="audio";break;case".jpg":case".jpeg":case".png":case".gif":case".bmp":case".ico":case".webp":case".svg":n="image";break;case".txt":n="text";break;case".md":n="md";break;case".doc":n="doc";break;case".docx":n="docx";break;case".ppt":n="ppt";break;case".pptx":n="pptx";break;case".pdf":n="pdf";break;case".xlsx":n="xlsx";break;case".csv":n="csv";break;case".py":case".js":case".ts":case".vue":case".jsx":case".tsx":case".java":case".cpp":case".c":case".cs":case".php":case".rb":case".go":case".rs":case".swift":case".kt":case".sh":case".json":case".toml":case".yml":case".yaml":case"tmol":case".sql":case".json":case".yml":case".yaml":case".html":case".xml":n="code";break;case".zip":case".rar":case".tar":case".gz":n="zip";break;case".folder":n="folder";break;default:n="unknown"}return n}function j(e){return x().FileIcons[w(e)]}function A(e){const s=e.substring(e.lastIndexOf(".")).toLowerCase(),n=w(s);switch(n){case"video":case"audio":case"image":return n;case"text":case"ppt":case"pptx":case"pdf":case"xlsx":case"csv":case"doc":case"docx":case"code":case"md":return"docs";default:return"others"}}const $=e=>{const s="/assets/icon/",n=e?"-dark":"";return{open:a("img",{src:`${s}yulan${n}.png`,width:"13",class:"inline"},null),detail:a("img",{src:`${s}xiangqing${n}.png`,width:"13",class:"inline"},null),cut:a("img",{src:`${s}jianqiewenjian${n}.png`,width:"13",class:"inline"},null),newFolder:a("img",{src:`${s}xinjianwenjianjia${n}.png`,width:"13",class:"inline"},null),deleteCompletely:a("img",{src:`${s}shanchu.png`,width:"13",class:"inline"},null),contextmenuLayout:a("img",{src:`${s}layout-5-line${n}.png`,width:"13",class:"inline"},null),listLayout:a("img",{src:`${s}liebiao${n}.png`,width:"13",class:"inline"},null),blockLayout:a("img",{src:`${s}yingyongzhongxin${n}.png`,width:"13",class:"inline"},null),treeLayout:a("img",{src:`${s}shuzhuangtu${n}.png`,width:"13",class:"inline"},null)}};function O(){const e=d(!1),s=()=>{e.value?(document.body.removeAttribute("arco-theme"),e.value=!1):(document.body.setAttribute("arco-theme","dark"),e.value=!0)},n=c(()=>e.value?"#fff":"#17171A"),t=c(()=>e.value?"#17171A":"#fff"),i=c(()=>e.value?"#fff":I),o=d(0),f=d(0),p=c(()=>q(e.value)),h=c(()=>$(e.value));return{isDark:e,toggleTheme:s,menuIdx:o,subMenuIdx:f,textColor:n,bgColor:t,iconColor:i,FileIcons:p,Icons:h}}const x=T("appStore",O,{persist:!0});var y=(e=>(e[e.NO_NEED=0]="NO_NEED",e[e.ING=1]="ING",e[e.OK=2]="OK",e[e.ERR=3]="ERR",e))(y||{});function z(e){return r({url:"/file/list",method:"GET",params:e})}function U(e){return r({url:"/file/transVideoCode",method:"POST",params:{id:e}})}function G(e){return r({url:"/file/transCodeStatus",method:"POST",data:{ids:e}})}function M(e){return r({url:"/file/uploadLyric",method:"POST",data:e})}function R(e){return r({url:"/file/detail",method:"GET",params:{id:e}})}function _(e){return r({url:"/file/folder",method:"post",data:e})}function K(e){return r({url:"/file/rename",method:"PUT",data:e})}function V(e,s){return r({url:"/file",method:"DELETE",data:{ids:e,complete:s}})}function H(e){return r({url:"/file/move",method:"PUT",data:e})}function W(e,s){return r({url:"/file/uploadChunk",method:"POST",data:e,signal:s,headers:{"Content-Type":"multipart/form-data"}})}function J(e){return r({url:"/file/deleteChunks",method:"DELETE",data:e})}function Q(e){return r({url:"/file/uploadedChunkIndexes",method:"GET",params:{md5:e}})}function X(e){return r({url:"/file/instantUpload",method:"POST",data:{pid:e.pid,md5:e.md5}})}function Y(e){return r({url:"/file/mergeChunks",method:"POST",data:e})}function Z(e){return r({url:"file/uploadFile",method:"POST",data:e,headers:{"Content-Type":"multipart/form-data"}})}function ee(e,s,n){return r({url:`/file/preview/${e}`,method:"GET",headers:{shareId:n??""},responseType:"blob",signal:s.signal})}function ne(e){return r({url:"/file/download",method:"POST",data:{fileIds:e},responseType:"blob"})}function P(){return r({url:"/file/folderLevelInfo",method:"GET"})}function se(e){return r({url:"/file/saveShareToMyPan",method:"POST",data:e})}const te={key:"id",title:"name",children:"children",icon:"icon",disabled:"disabled"},u="root",L=(e,s,n=!0)=>{for(const t of e){if(t.id===s||n&&s===""&&t.id===u)return t;if(t.children.length){const i=L(t.children,s);if(i)return i}}return null};function C(e,s,n,t=!0){for(const i of e){if(i.id===n||t&&n===""&&i.id===u)return s;if(i.children.length){const o=C(i.children,i,n);if(o)return o}}return null}const v=(e,s,n=!0)=>{for(const t of e){if(t.id===s||n&&s===""&&t.id===u)return[t];if(t.children.length){const i=v(t.children,s,n);if(i.length)return[t,...i]}}return[]};function E(e,s){var n;for(let t=e.length-1;t>=0;t--){const i=e[t];s.includes(e[t].id)?e.splice(t,1):(n=i.children)!=null&&n.length&&E(i.children,s)}}function N(e,s=[]){for(const n of e)s.push(n.id),n.children&&N(n.children,s);return s}function ae(){const e=d([]),s=x(),n=(l,m=!0)=>{const b=l.children?[...l.children.map(k=>n(k,m))]:[];return{...l,children:b,icon:()=>F(l.mediaType==="folder"?s.FileIcons.folder:j(l.name),{style:{minWidth:"16px"}}),disabled:m&&l.mediaType!=="folder"}},t=c(()=>e.value.map(l=>n(l))),i=c(()=>e.value.map(l=>n(l,!1))),o=c(()=>[{id:u,name:"/",children:t.value,icon:()=>s.FileIcons.folder,level:"",disabled:!1,status:y.NO_NEED,category:"all",mediaType:"folder",lyricPath:""}]),f=async()=>{try{const l=await P();l.code===200?e.value=l.data:g.error(l.message)}catch{g.error("获取文件层级失败")}},p=d([]),h=c(()=>v(i.value,p.value[0],!1));return{FAKE_ROOT_ID:u,rawLevel:e,treeDataBanFile:t,treeDataAllowFile:i,treeDataBanFileWithRoot:o,updateTreeLayerData:f,selectedIds:p,selectedPaths:h}}export{ne as A,U as B,C,u as F,y as T,w as a,W as b,Y as c,j as d,J as e,Q as f,A as g,X as h,L as i,N as j,G as k,ae as l,_ as m,K as n,V as o,H as p,z as q,Z as r,E as s,te as t,x as u,se as v,ee as w,$ as x,R as y,M as z};
