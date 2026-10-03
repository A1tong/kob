<template>
    <div class="matchground">
        <!-- 第一行：我（左） / 选择对手（中） / 对手（右），12 栅格等分成 4 + 4 + 4 -->
        <div class="row align-items-center g-0">
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.user.photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.user.username }}
                </div>
            </div>
            <div class="col-4">
                <div class="user-select-bot">
                    <select v-model="select_bot" class="form-select">
                        <option value="-1">亲自上阵</option>
                        <option v-for="bot in bots" :key="bot.id" :value="bot.id">
                            {{ bot.title }}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-4">
                <div class="user-photo">
                    <img :src="$store.state.pk.opponent_photo" alt="">
                </div>
                <div class="user-username">
                    {{ $store.state.pk.opponent_username }}
                </div>
            </div>
        </div>

        <!-- 第二行：开始匹配按钮 -->
        <div class="row g-0">
            <div class="col-12 text-center">
                <button @click="click_match_btn" type="button" class="btn btn-warning btn-lg">{{ match_btn_info }}</button>
            </div>
        </div>
    </div>
</template>

<script>

import { ref } from 'vue'
import { useStore } from 'vuex'
import $ from 'jquery'

export default {
    setup() {
        let match_btn_info = ref("开始匹配");
        let store = useStore();
        let bots = ref([]);
        let select_bot = ref("-1");  // 下拉框当前选中的值："-1"=亲自上阵，其余=bot 的 id

        const click_match_btn = () => {
            if (match_btn_info.value === "开始匹配") {
                match_btn_info.value = "取消";
                //send函数是浏览器自带的WebSocket方法，在kob的spring_boot说明文档中
                //说了浏览器和tomcat服务器建立了websocket链接，这个send函数就是与之
                //绑定的api，专门向这个通道里面塞消息
                store.state.pk.socket.send(JSON.stringify({
                    event: "start-matching",
                    bot_id: select_bot.value,   // 必须带上，否则后端 data.getInteger("bot_id") 是 null
                }));
            } else {
                match_btn_info.value = "开始匹配";
                store.state.pk.socket.send(JSON.stringify({
                    event: "stop-matching",
                }));
            }
        }

        const refresh_bots = () => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/bot/getlist/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    bots.value = resp;
                }
            })
        }

        refresh_bots(); // 从云端获取机器人列表，刷新bots数组

        return {
            match_btn_info,
            click_match_btn,
            bots,
            select_bot,
        }
    }

}
</script>

<style scoped>
div.matchground {
    /* 半透明“阴影框”本体：60vw 宽、70vh 高，水平居中 + 上下留白 */
    width: 60vw;
    height: 70vh;
    margin: 8vh auto;
    background-color: rgba(50, 50, 50, 0.5);   /* 前三个数是颜色，最后一个是透明度 */
    /* 框内纵向分上下两部分：上面一行（头像+下拉框）、下面一行（按钮） */
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 6vh 0 8vh;
}

div.user-photo {
    text-align: center;
}

div.user-photo > img {
    border-radius: 50%;
    width: 20vh;
    height: 20vh;          /* 宽高都写死，保证是正圆 */
    object-fit: cover;     /* 图片不是正方形时按圆裁切，不会压扁变形 */
}

div.user-username {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: white;
    padding-top: 2vh;
}

div.user-select-bot {
    text-align: center;
}

div.user-select-bot > select {
    width: 60%;
    max-width: 260px;
    margin: 0 auto;        /* 在中间那一列里水平居中 */
}
</style>
