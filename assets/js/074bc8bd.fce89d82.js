"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[698],{3905:function(t,e,n){n.d(e,{Zo:function(){return m},kt:function(){return u}});var a=n(7294);function r(t,e,n){return e in t?Object.defineProperty(t,e,{value:n,enumerable:!0,configurable:!0,writable:!0}):t[e]=n,t}function l(t,e){var n=Object.keys(t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(t);e&&(a=a.filter((function(e){return Object.getOwnPropertyDescriptor(t,e).enumerable}))),n.push.apply(n,a)}return n}function i(t){for(var e=1;e<arguments.length;e++){var n=null!=arguments[e]?arguments[e]:{};e%2?l(Object(n),!0).forEach((function(e){r(t,e,n[e])})):Object.getOwnPropertyDescriptors?Object.defineProperties(t,Object.getOwnPropertyDescriptors(n)):l(Object(n)).forEach((function(e){Object.defineProperty(t,e,Object.getOwnPropertyDescriptor(n,e))}))}return t}function o(t,e){if(null==t)return{};var n,a,r=function(t,e){if(null==t)return{};var n,a,r={},l=Object.keys(t);for(a=0;a<l.length;a++)n=l[a],e.indexOf(n)>=0||(r[n]=t[n]);return r}(t,e);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(t);for(a=0;a<l.length;a++)n=l[a],e.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(t,n)&&(r[n]=t[n])}return r}var p=a.createContext({}),d=function(t){var e=a.useContext(p),n=e;return t&&(n="function"==typeof t?t(e):i(i({},e),t)),n},m=function(t){var e=d(t.components);return a.createElement(p.Provider,{value:e},t.children)},c={inlineCode:"code",wrapper:function(t){var e=t.children;return a.createElement(a.Fragment,{},e)}},k=a.forwardRef((function(t,e){var n=t.components,r=t.mdxType,l=t.originalType,p=t.parentName,m=o(t,["components","mdxType","originalType","parentName"]),k=d(n),u=r,s=k["".concat(p,".").concat(u)]||k[u]||c[u]||l;return n?a.createElement(s,i(i({ref:e},m),{},{components:n})):a.createElement(s,i({ref:e},m))}));function u(t,e){var n=arguments,r=e&&e.mdxType;if("string"==typeof t||r){var l=n.length,i=new Array(l);i[0]=k;var o={};for(var p in e)hasOwnProperty.call(e,p)&&(o[p]=e[p]);o.originalType=t,o.mdxType="string"==typeof t?t:r,i[1]=o;for(var d=2;d<l;d++)i[d]=n[d];return a.createElement.apply(null,i)}return a.createElement.apply(null,n)}k.displayName="MDXCreateElement"},5970:function(t,e,n){n.r(e),n.d(e,{assets:function(){return m},contentTitle:function(){return p},default:function(){return u},frontMatter:function(){return o},metadata:function(){return d},toc:function(){return c}});var a=n(7462),r=n(3366),l=(n(7294),n(3905)),i=["components"],o={},p="Player",d={unversionedId:"api/functions/player",id:"version-3.1/api/functions/player",title:"Player",description:"updateOptions(options)",source:"@site/versioned_docs/version-3.1/api/functions/player.md",sourceDirName:"api/functions",slug:"/api/functions/player",permalink:"/docs/3.1/api/functions/player",editUrl:"https://github.com/doublesymmetry/react-native-track-player/tree/main/docs/versioned_docs/version-3.1/api/functions/player.md",tags:[],version:"3.1",frontMatter:{},sidebar:"app",previous:{title:"Lifecycle",permalink:"/docs/3.1/api/functions/lifecycle"},next:{title:"Queue",permalink:"/docs/3.1/api/functions/queue"}},m={},c=[{value:"<code>updateOptions(options)</code>",id:"updateoptionsoptions",level:2},{value:"<code>play()</code>",id:"play",level:2},{value:"<code>pause()</code>",id:"pause",level:2},{value:"<code>seekTo(seconds)</code>",id:"seektoseconds",level:2},{value:"<code>setVolume(volume)</code>",id:"setvolumevolume",level:2},{value:"<code>getVolume()</code>",id:"getvolume",level:2},{value:"<code>setRate(rate)</code>",id:"setraterate",level:2},{value:"<code>getRate()</code>",id:"getrate",level:2},{value:"<code>getDuration()</code>",id:"getduration",level:2},{value:"<code>getPosition()</code>",id:"getposition",level:2},{value:"<code>getBufferedPosition()</code>",id:"getbufferedposition",level:2},{value:"<code>getState()</code>",id:"getstate",level:2}],k={toc:c};function u(t){var e=t.components,n=(0,r.Z)(t,i);return(0,l.kt)("wrapper",(0,a.Z)({},k,n,{components:e,mdxType:"MDXLayout"}),(0,l.kt)("h1",{id:"player"},"Player"),(0,l.kt)("h2",{id:"updateoptionsoptions"},(0,l.kt)("inlineCode",{parentName:"h2"},"updateOptions(options)")),(0,l.kt)("p",null,"Updates the configuration for the components."),(0,l.kt)("p",null,"All parameters are optional. You also only need to specify the ones you want to update."),(0,l.kt)("p",null,"These parameters are different than the ones set using ",(0,l.kt)("inlineCode",{parentName:"p"},"setupPlayer()"),". Parameters other than those listed below will not be applied."),(0,l.kt)("p",null,"Some parameters are unused depending on platform."),(0,l.kt)("table",null,(0,l.kt)("thead",{parentName:"table"},(0,l.kt)("tr",{parentName:"thead"},(0,l.kt)("th",{parentName:"tr",align:null},"Param"),(0,l.kt)("th",{parentName:"tr",align:null},"Type"),(0,l.kt)("th",{parentName:"tr",align:null},"Description"),(0,l.kt)("th",{parentName:"tr",align:"center"},"Android"),(0,l.kt)("th",{parentName:"tr",align:"center"},"iOS"),(0,l.kt)("th",{parentName:"tr",align:"center"},"Windows"))),(0,l.kt)("tbody",{parentName:"table"},(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"MetadataOptions")),(0,l.kt)("td",{parentName:"tr",align:null},"The options"),(0,l.kt)("td",{parentName:"tr",align:"center"}),(0,l.kt)("td",{parentName:"tr",align:"center"}),(0,l.kt)("td",{parentName:"tr",align:"center"})),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.ratingType"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/constants/rating"},"RatingType")),(0,l.kt)("td",{parentName:"tr",align:null},"The rating type"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.forwardJumpInterval"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The interval in seconds for the jump forward buttons (if only one is given then we use that value for both)"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.backwardJumpInterval"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The interval in seconds for the jump backward buttons (if only one is given then we use that value for both)"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.stoppingAppPausesPlayback"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"boolean")),(0,l.kt)("td",{parentName:"tr",align:null},"Whether the player will pause playback when the app closes"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.alwaysPauseOnInterruption"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"boolean")),(0,l.kt)("td",{parentName:"tr",align:null},"Whether the ",(0,l.kt)("inlineCode",{parentName:"td"},"remote-duck")," event will be triggered on every interruption"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.likeOptions"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/feedback"},"FeedbackOptions")),(0,l.kt)("td",{parentName:"tr",align:null},"The media controls that will be enabled"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.dislikeOptions"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/feedback"},"FeedbackOptions")),(0,l.kt)("td",{parentName:"tr",align:null},"The media controls that will be enabled"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.bookmarkOptions"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/feedback"},"FeedbackOptions")),(0,l.kt)("td",{parentName:"tr",align:null},"The media controls that will be enabled"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.capabilities"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/constants/capability"},"Capability[]")),(0,l.kt)("td",{parentName:"tr",align:null},"The media controls that will be enabled"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.notificationCapabilities"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/constants/capability"},"Capability[]")),(0,l.kt)("td",{parentName:"tr",align:null},"The buttons that it will show in the notification. Defaults to ",(0,l.kt)("inlineCode",{parentName:"td"},"data.capabilities")),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.compactCapabilities"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/constants/capability"},"Capability[]")),(0,l.kt)("td",{parentName:"tr",align:null},"The buttons that it will show in the compact notification"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.icon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The notification icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.playIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The play icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.pauseIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The pause icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.stopIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The stop icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.previousIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The previous icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.nextIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The next icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.rewindIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The jump backward icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.forwardIcon"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/objects/resource"},"Resource Object")),(0,l.kt)("td",{parentName:"tr",align:null},"The jump forward icon\xb9"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.color"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The notification color in an ARGB hex"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"options.progressUpdateEventInterval"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The interval (in seconds) that the ",(0,l.kt)("a",{parentName:"td",href:"/docs/3.1/api/events#playbackprogressupdated"},(0,l.kt)("inlineCode",{parentName:"a"},"Event.PlaybackProgressUpdated"))," will be fired. ",(0,l.kt)("inlineCode",{parentName:"td"},"undefined")," by default."),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:"center"},"\u274c")))),(0,l.kt)("p",null,(0,l.kt)("em",{parentName:"p"},"\xb9 - The custom icons will only work in release builds")),(0,l.kt)("h2",{id:"play"},(0,l.kt)("inlineCode",{parentName:"h2"},"play()")),(0,l.kt)("p",null,"Plays or resumes the current track."),(0,l.kt)("h2",{id:"pause"},(0,l.kt)("inlineCode",{parentName:"h2"},"pause()")),(0,l.kt)("p",null,"Pauses the current track."),(0,l.kt)("h2",{id:"seektoseconds"},(0,l.kt)("inlineCode",{parentName:"h2"},"seekTo(seconds)")),(0,l.kt)("p",null,"Seeks to a specified time position in the current track."),(0,l.kt)("table",null,(0,l.kt)("thead",{parentName:"table"},(0,l.kt)("tr",{parentName:"thead"},(0,l.kt)("th",{parentName:"tr",align:null},"Param"),(0,l.kt)("th",{parentName:"tr",align:null},"Type"),(0,l.kt)("th",{parentName:"tr",align:null},"Description"))),(0,l.kt)("tbody",{parentName:"table"},(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"seconds"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The position in seconds")))),(0,l.kt)("h2",{id:"setvolumevolume"},(0,l.kt)("inlineCode",{parentName:"h2"},"setVolume(volume)")),(0,l.kt)("p",null,"Sets the volume of the player."),(0,l.kt)("table",null,(0,l.kt)("thead",{parentName:"table"},(0,l.kt)("tr",{parentName:"thead"},(0,l.kt)("th",{parentName:"tr",align:null},"Param"),(0,l.kt)("th",{parentName:"tr",align:null},"Type"),(0,l.kt)("th",{parentName:"tr",align:null},"Description"))),(0,l.kt)("tbody",{parentName:"table"},(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"volume"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The volume in a range from 0 to 1")))),(0,l.kt)("h2",{id:"getvolume"},(0,l.kt)("inlineCode",{parentName:"h2"},"getVolume()")),(0,l.kt)("p",null,"Gets the volume of the player (a number between 0 and 1)."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<number>")),(0,l.kt)("h2",{id:"setraterate"},(0,l.kt)("inlineCode",{parentName:"h2"},"setRate(rate)")),(0,l.kt)("p",null,"Sets the playback rate"),(0,l.kt)("table",null,(0,l.kt)("thead",{parentName:"table"},(0,l.kt)("tr",{parentName:"thead"},(0,l.kt)("th",{parentName:"tr",align:null},"Param"),(0,l.kt)("th",{parentName:"tr",align:null},"Type"),(0,l.kt)("th",{parentName:"tr",align:null},"Description"))),(0,l.kt)("tbody",{parentName:"table"},(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"rate"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"number")),(0,l.kt)("td",{parentName:"tr",align:null},"The playback rate where 1 is the regular speed")))),(0,l.kt)("h2",{id:"getrate"},(0,l.kt)("inlineCode",{parentName:"h2"},"getRate()")),(0,l.kt)("p",null,"Gets the playback rate, where 1 is the regular speed."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<number>")),(0,l.kt)("h2",{id:"getduration"},(0,l.kt)("inlineCode",{parentName:"h2"},"getDuration()")),(0,l.kt)("p",null,"Gets the duration of the current track in seconds."),(0,l.kt)("p",null,"Note: ",(0,l.kt)("inlineCode",{parentName:"p"},"react-native-track-player")," is a streaming library, which means it slowly buffers the track and doesn't know exactly when it ends.\nThe duration returned by this function is determined through various tricks and ",(0,l.kt)("em",{parentName:"p"},"may not be exact or may not be available at all"),"."),(0,l.kt)("p",null,"You should only trust the result of this function if you included the ",(0,l.kt)("inlineCode",{parentName:"p"},"duration")," property in the ",(0,l.kt)("a",{parentName:"p",href:"/docs/3.1/api/objects/track"},"Track Object"),"."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<number>")),(0,l.kt)("h2",{id:"getposition"},(0,l.kt)("inlineCode",{parentName:"h2"},"getPosition()")),(0,l.kt)("p",null,"Gets the position of the current track in seconds."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<number>")),(0,l.kt)("h2",{id:"getbufferedposition"},(0,l.kt)("inlineCode",{parentName:"h2"},"getBufferedPosition()")),(0,l.kt)("p",null,"Gets the buffered position of the current track in seconds."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<number>")),(0,l.kt)("h2",{id:"getstate"},(0,l.kt)("inlineCode",{parentName:"h2"},"getState()")),(0,l.kt)("p",null,"Gets the playback ",(0,l.kt)("a",{parentName:"p",href:"/docs/3.1/api/constants/state"},(0,l.kt)("inlineCode",{parentName:"a"},"State"))," of the player."),(0,l.kt)("p",null,(0,l.kt)("strong",{parentName:"p"},"Returns:")," ",(0,l.kt)("inlineCode",{parentName:"p"},"Promise<"),(0,l.kt)("a",{parentName:"p",href:"/docs/3.1/api/constants/state"},"State"),(0,l.kt)("inlineCode",{parentName:"p"},">")))}u.isMDXComponent=!0}}]);