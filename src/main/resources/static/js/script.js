document.addEventListener('DOMContentLoaded', function() {

    /**
     * 用于消息提示
     */
    function showMessage(message, isError = false) {
        // 创建一个新的div元素作为提示框
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.className = `toast-message ${isError ? 'error' : 'success'}`;
        document.body.appendChild(toast);

        // 触发显示动画
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

        // 3秒后自动移除提示框
        setTimeout(() => {
            toast.classList.remove('show');
            toast.addEventListener('transitionend', () => toast.remove());
        }, 3000);
    }

    /**
     * 用于显示或隐藏密码
     */
    function setupPasswordToggles() {
        const togglePasswordIcons = document.querySelectorAll('.password-toggle-icon');

        // 定义一个睁开的眼睛和一个闭上的眼睛
        const eyeIconSvg = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
        </svg>`;
        const eyeSlashIconSvg = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/>
        </svg>`;

        togglePasswordIcons.forEach(icon => {
            // 开始时默认隐藏密码
            icon.innerHTML = eyeSlashIconSvg;

            icon.addEventListener('click', function() {
                const passwordInput = this.previousElementSibling;

                // 转换逻辑
                if (passwordInput.type === 'text') {
                    passwordInput.type = 'password';
                    this.innerHTML = eyeSlashIconSvg;
                } else {
                    passwordInput.type = 'text';
                    this.innerHTML = eyeIconSvg;
                }
            });
        });
    }

    // 初始化密码切换功能
    setupPasswordToggles();

    /**
     * 处理注册填写的表单
     */
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            event.preventDefault();// 组织默认行为

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const nickname = document.getElementById('nickname').value;
            const email = document.getElementById('email').value;
            const phone = document.getElementById('phone').value;

            //验证密码
            if (password !== confirmPassword) {
                showMessage('两次输入的密码不一致，请重新输入！', true);
                return;
            }

            const userData = {
                username: username,
                password: password,
                nickname: nickname || `用户${Math.random().toString(36).substr(2, 8)}`, // 如果昵称为空，生成一个随机昵称
                email: email,
                phone: phone
            };

            // 使用 fetch API 发送POST请求到后端
            fetch('/api/users/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            })
                .then(response => {
                    return response.json().then(data => {
                        return { ok: response.ok, data: data };
                    });
                })
                .then(({ ok, data }) => {
                    if (ok) {
                        showMessage('注册成功！即将跳转到登录页面...');
                        // 2秒后跳转到登录页面
                        setTimeout(() => {
                            window.location.href = '/login.html';
                        }, 2000);
                    } else {
                        showMessage(data.error || '注册失败，请稍后再试。', true);
                    }
                })
                // 捕捉其他原因导致的注册失败，比如网络问题
                .catch(error => {
                    console.error('注册请求失败:', error);
                    showMessage('无法连接到服务器，请检查您的网络。', true);
                });
        });
    }

    /**
     * 处理登录填写的表单
     */
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(event) {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const credentials = {
                username: username,
                password: password
            };

            fetch('/api/users/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(credentials)
            })
                .then(response => response.json().then(data => ({ ok: response.ok, data: data })))
                .then(({ ok, data }) => {
                    if (ok) {
                        showMessage('登录成功！正在跳转到主页...');
                        // 将返回的用户信息存储在浏览器的localStorage中，这样其他页面就可以知道用户是登录状态
                        localStorage.setItem('loggedInUser', JSON.stringify(data));
                        // 1.5秒后跳转到首页
                        setTimeout(() => {
                            window.location.href = '/index.html';
                        }, 1500);
                    } else {
                        showMessage(data.error || '登录失败，请检查您的登录名和密码。', true);
                    }
                })
                .catch(error => {
                    console.error('登录请求失败:', error);
                    showMessage('无法连接到服务器，请检查您的网络。', true);
                });
        });
    }

});