<template>
    <PlayGround v-if="$store.state.pk.status === 'playing'" />
    <MatchGround v-if="$store.state.pk.status === 'matching'" />
    <ResultBoard v-if="$store.state.pk.loser != 'none'" />
</template>

<script>
import PlayGround from '../../components/PlayGround.vue'
import MatchGround from '../../components/MatchGround.vue'
import ResultBoard from '../../components/ResultBoard.vue'
import { onMounted, onUnmounted } from 'vue' // 前者是组件被挂载后执行的函数，后者是组件被卸载时执行的函数
import { useStore } from 'vuex'

export default {
    components: {
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup() {
        const store = useStore();
        const socketUrl = `ws://127.0.0.1:3000/websocket/${store.state.user.token}/`;

        store.commit("updateLoser", "none");

        let socket = null;
        onMounted(() => { // 这里的挂载指的是界面被打开
            store.commit("updateOpponent", { // 因为用的是commit，所以会从仓库的mutations中找，没有找到，就递归的向下一层仓库的mutations中找
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png"
            })
            socket = new WebSocket(socketUrl);

            // 这个函数是浏览器收到tomcat服务器发来的状态码101时，调用的
            // 因为当浏览器收到101状态码，之前请求创建websocket通道的http
            // 协议通道就立马转换成websocket协议通道，链接也是这一瞬间创建完毕的
            socket.onopen = () => {
                console.log("connected!");
                store.commit("updateSocket", socket);
            }

            // 接收后端返回信息
            socket.onmessage = msg => {
                const data = JSON.parse(msg.data);
                if (data.event === "start-matching") { // 匹配成功
                    store.commit("updateOpponent", {
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(() => {
                        store.commit("updateStatus", "playing");
                    }, 200);
                    store.commit("updateGame", data.game);
                } else if (data.event === "move") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    if (!game) return; // 判空
                    if (data.a_direction === undefined ||
                        data.b_direction === undefined) return; // 方向缺失保护
                    const [snake0, snake1] = game.snakes;
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                } else if (data.event === "result") {
                    console.log(data);
                    const game = store.state.pk.gameObject;
                    if (!game) return; // 判空
                    const [snake0, snake1] = game.snakes;

                    if (data.loser === "all" || data.loser === "A") {
                        snake0.status = "die";
                        snake0.direction = -1;
                    }
                    if (data.loser === "all" || data.loser === "B") {
                        snake1.status = "die";
                        snake1.direction = -1;
                    }
                    store.commit("updateLoser", data.loser);
                }
            }

            // 只要链接断开，前后端的该函数都会触发
            socket.onclose = () => {
                console.log("disconnected!");
            }
        })

        onUnmounted(() => { // 这里被卸载指的是，界面被关闭
            socket.close();
            store.commit("updateStatus", "matching");
        })
    }
}
</script>

<style scoped>
</style>