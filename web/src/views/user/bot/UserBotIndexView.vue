<template>
    <ContentField>
        我的Bot
    </ContentField>
</template>

<script>
import ContentField from '../../../components/ContentField.vue'
import $ from 'jquery'
import { useStore } from 'vuex'

export default {
    components: {
        ContentField
    },
    setup() { 
        // 浏览器会把请求发给后端，而后端把响应发给浏览器，浏览器收到响应后要么把
        // XMLHttpRequest.status = 0，要么把响应丢给XMLHttpRequest实例
        // 后续执行success还是error，由jQuery这个全局实例对象决定
        // (jQuery的JavaScript代码被浏览器加载后，就在浏览器内存里被创建了)
        const store = useStore();
        /*$.ajax({
            url: "http://127.0.0.1:3000/user/bot/add/",
            type: "post",
            data: {
                title: "Bot的标题",
                description: "Bot的描述",
                content: "Bot的代码",
            },
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            success(resp) {
                console.log(resp);
            },
            error(resp) {
                console.log(resp);
            }
        })*/
        /*$.ajax({
            url: "http://127.0.0.1:3000/user/bot/remove/",
            type: "post",
            data: {
                bot_id: 1,
            },
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            success(resp) {
                console.log(resp);
            },
            error(resp) {
                console.log(resp);
            }
        })*/
        /*$.ajax({
            url: "http://127.0.0.1:3000/user/bot/update/",
            type: "post",
            data: {
                bot_id: 1,
                title: "you fuck",
                description: "you fuck",
                content: "you fuck",
            },
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            success(resp) {
                console.log(resp);
            },
            error(resp) {
                console.log(resp);
            }
        })*/
        $.ajax({
            url: "http://127.0.0.1:3000/user/bot/getlist/",
            type: "get",
            headers: {
                Authorization: "Bearer " + store.state.user.token,
            },
            success(resp) {
                console.log(resp);
            },
            error(resp) {
                console.log(resp);
            }
        })
    }
}
</script>

<style scoped>
</style>