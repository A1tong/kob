const AC_GAME_OBJECTS = [];

export class AcGameObject {
    constructor() {
        AC_GAME_OBJECTS.push(this);
        this.has_called_start = false; // 是否执行过 start 函数
        this.timedelta = 0; // 当前帧距离上一帧的时间间隔，单位是 ms
    }

    start() { // 只在第一帧时执行
        
    }

    update() { // 每一帧都执行，除了第一帧 
        
    }
    
    on_destroy() { // 在被销毁前执行

    }

    destroy() { // 销毁对象
        this.on_destroy();
        
        for (let i in AC_GAME_OBJECTS) {
            const obj = AC_GAME_OBJECTS[i];
            if (obj === this) {
                AC_GAME_OBJECTS.splice(i, 1);
                break;
            }
        }
    }
}

let last_timestamp; // 上一帧的时间戳
const step = timestamp => {
    for (let obj of AC_GAME_OBJECTS) {
        if (!obj.has_called_start) {
            obj.start();
            obj.has_called_start = true;
        } else {
            obj.timedelta = timestamp - last_timestamp;
            obj.update();
        }
    }

    last_timestamp = timestamp;
    requestAnimationFrame(step);
}

requestAnimationFrame(step);