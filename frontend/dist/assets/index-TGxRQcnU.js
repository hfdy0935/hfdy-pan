/* empty css              *//* empty css              *//* empty css              *//* empty css              *//* empty css              */import{o as B,M as r,B as j,q as M,a8 as U,N as $,a5 as L,a9 as V}from"./index-fDEUsWmJ.js";import{u as A,d as G,w as K,t as P,i as W}from"./useFolderLevelTree-CDJ_26ol.js";import{d as R,h as D,b as i,c as f,e as Y,C as x,k as c,j as y,i as T,l as z,$ as s,B as H,r as F,y as J,Y as E,F as Q}from"./vue-office_docx-BTzFQIdC.js";import"./index-DeLZq7_u.js";function X(){return B({url:"/recycle",method:"GET"})}function Z(m,d){return B({url:"/recycle",method:"DELETE",data:{ids:m,complete:d}})}function ee(m,d){return B({url:"/recycle/recover",method:"PUT",data:{ids:m,pid:d}})}const te={class:"min-h-14 flex items-center justify-center"},ae={key:0},ne={key:1,class:"w-64 flex justify-evenly"},oe={key:2,class:"w-36 ml-12 flex justify-start items-center"},se={key:3},re=R({__name:"table-cell",props:{column:{},record:{},rowIndex:{}},emits:["update","open"],setup(m,{emit:d}){const _=d,n=A(),w=async()=>{try{const e=await Z([m.record.id],!0);e.code===200?(r.success("已彻底删除"),_("update",m.record.id)):r.error(e.message)}catch{r.error("删除失败")}};return(e,h)=>{const p=D("icon-refresh"),u=j,I=D("icon-delete"),g=M;return i(),f("td",null,[Y("div",te,[e.column.dataIndex==="index"?(i(),f("div",ae,x(e.rowIndex+1),1)):e.column.dataIndex==="operation"?(i(),f("div",ne,[c(u,{type:"primary",status:"success",onClick:h[0]||(h[0]=b=>e.$emit("open",e.record))},{icon:y(()=>[c(p)]),_:1}),c(g,{type:"warning",position:"lt",content:"确定要彻底删除该文件/文件夹吗？",onOk:w},{default:y(()=>[c(u,{type:"primary",status:"danger"},{icon:y(()=>[c(I)]),_:1})]),_:1})])):e.column.dataIndex==="name"?(i(),f("div",oe,[(i(),T(z(e.record.mediaType==="folder"?s(n).FileIcons.folder:s(G)(e.record.name)),{class:"mr-2 scale-150"})),H(" "+x(e.record.name),1)])):(i(),f("div",se,x(e.record[e.column.dataIndex]),1))])])}}}),ye=R({__name:"index",setup(m){const{FAKE_ROOT_ID:d,treeDataBanFileWithRoot:_,selectedIds:n,updateTreeLayerData:w}=K(),e=F([]);J(async()=>{try{const a=await X();a.code===200?e.value=a.data:r.error(a.message)}catch{r.error("获取回收站信息失败")}});const h=[{title:"序号",dataIndex:"index",width:60},{title:"文件名",dataIndex:"name",width:180},{title:"路径",dataIndex:"level",width:180},{title:"创建时间",dataIndex:"createTime",width:180},{title:"删除时间",dataIndex:"deleteTime",width:180},{title:"操作",dataIndex:"operation",width:120}].map(a=>({...a,align:"center"})),p=F(!1),u=F(),I=async a=>{var o;await w();const t=W(_.value,a.pid);n.value=[a.pid||d],t||(n.value=[]),u.value=a,p.value=!0,t&&((o=t.children)!=null&&o.map(l=>l.name).includes(a.name))&&(n.value=[])},g=(a,{node:t})=>{var l;const o=(l=t.children)==null?void 0:l.map(k=>k.name);o!=null&&o.includes(u.value.name)&&(r.warning("同一目录下的文件/文件夹名不能重复"),n.value=[])},b=async()=>{if(!n.value.length)return r.warning("请选择要恢复到哪个文件夹"),!1;try{const a=n.value[0]===d?"":n.value[0],t=await ee([u.value.id],a);t.code===200?(r.success("恢复成功"),e.value=e.value.filter(o=>{var l;return o.id!==((l=u.value)==null?void 0:l.id)})):r.error(t.message)}catch{r.error("恢复失败")}};return(a,t)=>{const o=U,l=L,k=V,C=$;return i(),f(Q,null,[s(e).length?(i(),T(o,{key:0,columns:s(h),class:"m-3 mt-10",scroll:{x:700},data:s(e),hoverable:"",pagination:!1},{td:y(({column:v,record:N,rowIndex:q})=>[c(re,{column:v,record:N,rowIndex:q,onUpdate:t[0]||(t[0]=O=>e.value=s(e).filter(S=>S.id!==O)),onOpen:I},null,8,["column","record","rowIndex"])]),_:1},8,["columns","data"])):(i(),T(l,{key:1,class:"mt-36"})),c(C,{visible:s(p),"onUpdate:visible":t[2]||(t[2]=v=>E(p)?p.value=v:null),onOk:b,title:"选择恢复到哪个文件夹"},{default:y(()=>[c(k,{data:s(_),fieldNames:s(P),"selected-keys":s(n),"onUpdate:selectedKeys":t[1]||(t[1]=v=>E(n)?n.value=v:null),onSelect:g,"check-strictly":""},null,8,["data","fieldNames","selected-keys"])]),_:1},8,["visible"])],64)}}});export{ye as default};
