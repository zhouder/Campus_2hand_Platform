document.addEventListener('DOMContentLoaded', function() {
    // 使用 Map 来存储待上传的 File 对，方便添加和删除
    const uploadedFiles = new Map();

    /**
     * 用于消息提示
     */
    function showMessage(message, isError = false) {
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.className = `toast-message ${isError ? 'error' : 'success'}`;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.classList.add('show');
        }, 100);

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
        const eyeIconSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/></svg>`;
        const eyeSlashIconSvg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/></svg>`;
        togglePasswordIcons.forEach(icon => {
            icon.innerHTML = eyeSlashIconSvg;
            icon.addEventListener('click', function() {
                const passwordInput = this.previousElementSibling;
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
    setupPasswordToggles();

    /**
     * 处理注册填写的表单
     */
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const nickname = document.getElementById('nickname').value;
            const email = document.getElementById('email').value;
            const phone = document.getElementById('phone').value;
            if (password !== confirmPassword) {
                showMessage('两次输入的密码不一致，请重新输入！', true);
                return;
            }
            const userData = { username: username, password: password, nickname: nickname || `用户${Math.random().toString(36).substr(2, 8)}`, email: email, phone: phone };
            fetch('/api/users/register', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(userData) })
                .then(response => response.json().then(data => ({ ok: response.ok, data: data })))
                .then(({ ok, data }) => {
                    if (ok) {
                        showMessage('注册成功！即将跳转到登录页面...');
                        setTimeout(() => { window.location.href = '/login.html'; }, 2000);
                    } else {
                        showMessage(data.error || '注册失败，请稍后再试。', true);
                    }
                }).catch(error => {
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
            const credentials = { username: username, password: password };
            fetch('/api/users/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(credentials) })
                .then(response => response.json().then(data => ({ ok: response.ok, data: data })))
                .then(({ ok, data }) => {
                    if (ok) {
                        showMessage('登录成功！正在跳转到主页...');
                        localStorage.setItem('loggedInUser', JSON.stringify(data));
                        setTimeout(() => { window.location.href = '/index.html'; }, 1500);
                    } else {
                        showMessage(data.error || '登录失败，请检查您的登录名和密码。', true);
                    }
                }).catch(error => {
                console.error('登录请求失败:', error);
                showMessage('无法连接到服务器，请检查您的网络。', true);
            });
        });
    }

    /**
     * 处理发布商品的表单
     */
    const publishForm = document.getElementById('publishForm');
    if (publishForm) {
        publishForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
            if (!loggedInUser) {
                showMessage('请先登录再发布商品！', true);
                setTimeout(() => { window.location.href = '/login.html'; }, 2000);
                return;
            }
            if (uploadedFiles.size === 0) {
                showMessage('请至少上传一张商品图片！', true);
                return;
            }
            const formData = new FormData();
            formData.append('title', document.getElementById('title').value);
            formData.append('price', document.getElementById('price').value);
            formData.append('description', document.getElementById('description').value);
            formData.append('category', document.getElementById('category').value);
            formData.append('location', document.getElementById('location').value);
            formData.append('sellerId', loggedInUser.id);
            uploadedFiles.forEach(file => {
                formData.append('images', file);
            });
            fetch('/api/products', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json().then(data => ({ ok: response.ok, data: data })))
                .then(({ ok, data }) => {
                    if (ok) {
                        showMessage('商品发布成功！即将跳转到首页...');
                        setTimeout(() => {
                            window.location.href = '/index.html';
                        }, 2000);
                    } else {
                        showMessage(data.error || '商品发布失败，请稍后再试。', true);
                    }
                })
                .catch(error => {
                    console.error('发布请求失败:', error);
                    showMessage('无法连接到服务器，请检查您的网络。', true);
                });
        });
    }

    /**
     * 图片上传预览和 Lightbox 功能
     */
    const imageUpload = document.getElementById('imageUpload');
    const imagePreviewGrid = document.getElementById('imagePreviewGrid');
    const uploadBox = document.getElementById('uploadBox');
    const lightbox = document.getElementById('lightbox');
    const lightboxImg = document.getElementById('lightboxImg');
    const lightboxClose = document.querySelector('.lightbox-close');

    if (imageUpload && imagePreviewGrid && uploadBox) {
        imageUpload.addEventListener('change', function(event) {
            const files = event.target.files;
            if (!files) return;

            const maxFiles = 9;
            if (uploadedFiles.size + files.length > maxFiles) {
                showMessage(`最多只能上传 ${maxFiles} 张图片！`, true);
                return;
            }

            for (const file of files) {
                const uniqueId = Date.now() + Math.random();
                uploadedFiles.set(uniqueId, file);

                const reader = new FileReader();
                reader.onload = function(e) {
                    const previewContainer = document.createElement('div');
                    previewContainer.className = 'image-preview-item';

                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.addEventListener('click', () => {
                        lightboxImg.src = e.target.result;
                        lightbox.classList.add('show');
                    });

                    const removeBtn = document.createElement('span');
                    removeBtn.className = 'remove-image-btn';
                    removeBtn.innerHTML = '&times;';
                    removeBtn.onclick = (ev) => {
                        ev.stopPropagation();
                        uploadedFiles.delete(uniqueId);
                        previewContainer.remove();
                        if (uploadedFiles.size < maxFiles) {
                            uploadBox.style.display = 'flex';
                        }
                    };

                    previewContainer.appendChild(img);
                    previewContainer.appendChild(removeBtn);
                    imagePreviewGrid.insertBefore(previewContainer, uploadBox);
                }
                reader.readAsDataURL(file);
            }

            if (uploadedFiles.size >= maxFiles) {
                uploadBox.style.display = 'none';
            }
            imageUpload.value = '';
        });
    }

    if (lightbox && lightboxClose) {
        const closeLightbox = () => lightbox.classList.remove('show');
        lightboxClose.addEventListener('click', closeLightbox);
        lightbox.addEventListener('click', (e) => {
            if (e.target === lightbox) {
                closeLightbox();
            }
        });
    }
});