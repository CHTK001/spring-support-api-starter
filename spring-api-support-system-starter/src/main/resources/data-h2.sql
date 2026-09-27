/*
 * 闁氨鏁ら張宥呭濡€虫健 - 閺佺増宓佹惔鎾冲灥婵瀵查懘姘拱
 * 
 * 閸栧懎鎯堢悰?
 *   - sys_bank_bins: 闁炬儼顢態IN鐎靛湱鍙庣悰? *   - sys_city: 鐞涘本鏂傞崠鍝勫灊鐞? *   - sys_dict: 鐎涙鍚€鐞? *   - sys_dict_item: 鐎涙鍚€鐎涙劙銆嶇悰? *   - sys_dict_item_property: 鐎涙鍚€闁板秶鐤嗘い纭呫€? *   - sys_log: 缁崵绮洪幙宥勭稊閺冦儱绻旂悰? *   - sys_setting_group: 缁崵绮洪柊宥囩枂閸掑棛绮嶇悰? *   - sys_setting: 缁崵绮洪崗銊ョ湰闁板秶鐤嗙悰? *   - sys_setting_history: 缁崵绮洪柊宥囩枂閸欐ɑ娲块崢鍡楀蕉鐞? *   - sys_limit_configuration: 闂勬劖绁﹂柊宥囩枂鐞? *   - sys_limit_record: 闂勬劖绁︾拋鏉跨秿鐞? *   - sys_holiday: 閼哄倸浜ｉ弮銉ㄣ€? *   - sys_file_system_group: 閺傚洣娆㈢化鑽ょ埠閸掑棛绮嶇悰? *   - sys_file_system_setting: 閺傚洣娆㈢化鑽ょ埠闁板秶鐤嗙悰? *   - sys_file_system: 閺傚洣娆㈢化鑽ょ埠娑撴槒銆? *   - sys_file_system_part: 閺傚洣娆㈢化鑽ょ埠閸掑棛澧栫悰? *
 * 閹绘劒绶电化鑽ょ埠闁氨鏁ら惃鍕摟閸忓摜顓搁悶鍡愨偓渚€鍘ょ純顔绢吀閻炲棎鈧焦妫╄箛妤勵唶瑜版洏鈧焦绉烽幁顖炩偓姘辩叀閵嗕線妾哄ù浣瑰付閸掕翰鈧焦鏋冩禒鍓侇吀閻炲棛鐡戦崝鐔诲厴
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */




-- =====================================================
-- 1. 闁炬儼顢態IN鐎靛湱鍙庣悰?(sys_bank_bins)
-- 閻劑鈧? 閺嶈宓侀柧鎯邦攽閸椻€冲娇閸撳秶绱戠拠鍡楀焼閸欐垵宕遍柧鎯邦攽閸滃苯宕辩猾璇茬€?-- =====================================================
DROP TABLE IF EXISTS sys_bank_bins;
CREATE TABLE sys_bank_bins (
    sys_blank_bin_id int NOT NULL AUTO_INCREMENT COMMENT 'BIN鐠佹澘缍岻D - 娑撳鏁懛顏勵杻',
    sys_blank_bin_bank_name varchar(20)   NOT NULL DEFAULT '' COMMENT '闁炬儼顢戦崥宥囆?- 閸欐垵宕遍柧鎯邦攽娑擃厽鏋冮崥宥囆?,
    sys_blank_bin_bank_name_en varchar(20)   NULL DEFAULT NULL COMMENT '闁炬儼顢戦懟杈ㄦ瀮缁犫偓缁?- 婵′痉CBC閵嗕竼CB缁?,
    sys_blank_bin_card_name varchar(20)   NULL DEFAULT NULL COMMENT '閸楋紕澧栭崥宥囆?- 闁炬儼顢戦崡鈥查獓閸濅礁鎮曠粔?,
    sys_blank_bin_card_type varchar(20)   NULL DEFAULT NULL COMMENT '閸楋紕澧栫猾璇茬€?- 閸婄喕顔囬崡?娣囷紕鏁ら崡锛勭搼',
    sys_blank_bin_bin varchar(20)   NOT NULL DEFAULT '' COMMENT 'BIN閸?- 闁炬儼顢戦崡鈥冲娇閸?娴ｅ秵鐖ｇ拠鍡欑垳',
    sys_blank_bin_n_length tinyint NULL DEFAULT NULL COMMENT '閸椻€冲娇闂€鍨 - 鐎瑰本鏆ｉ柧鎯邦攽閸椻€冲娇閻ㄥ嫪缍呴弫?,
    sys_blank_bin_bin_length tinyint NULL DEFAULT NULL COMMENT 'BIN闂€鍨 - BIN閸欓娈戞担宥嗘殶',
    sys_blank_bin_issue_id int NULL DEFAULT NULL COMMENT '閸欐垵宕遍張鐑樼€疘D - 閸欐垵宕遍柧鎯邦攽閻ㄥ嫭鐖ｇ拠鍜篋',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_blank_bin_id) USING BTREE,
    INDEX idx_bin (sys_blank_bin_bin) USING BTREE COMMENT 'BIN閸欓鍌ㄥ?- 閻劋绨箛顐︹偓鐔峰爱闁板秹鎽辩悰灞藉幢',
    INDEX idx_bank_name (sys_blank_bin_bank_name) USING BTREE COMMENT '闁炬儼顢戦崥宥囆炵槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci 
COMMENT = '闁炬儼顢態IN鐎靛湱鍙庣悰?- 閺嶈宓侀柧鎯邦攽閸椻€冲缂傗偓鐠囧棗鍩嗛崣鎴濆幢闁炬儼顢? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 鐎涙鍚€鐞?(sys_dict)
-- 閻劑鈧? 缁崵绮洪弫鐗堝祦鐎涙鍚€閿涘瞼顓搁悶鍡楁倗缁粯鐏囨稉鎯р偓鐓庢嫲娑撳濯洪柅澶愩€?-- =====================================================
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    sys_dict_id int NOT NULL AUTO_INCREMENT COMMENT '鐎涙鍚€ID - 娑撳鏁懛顏勵杻',
    sys_dict_name varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€閸氬秶袨 - 鐎涙鍚€閻ㄥ嫭妯夌粈鍝勬倳缁?,
    sys_dict_code varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€缂傛牜鐖?- 鐎涙鍚€閻ㄥ嫬鏁稉鈧弽鍥槕閻緤绱濋悽銊ょ艾缁嬪绨拫鍐暏',
    sys_dict_pid int NULL DEFAULT 0 COMMENT '閻栬泛鐡ч崗绐盌 - 閺€顖涘瘮鐎涙鍚€鐏炲倻楠囩紒鎾寸€敍?鐞涖劎銇氭い鍓侀獓鐎涙鍚€',
    sys_dict_status int NULL DEFAULT 1 COMMENT '鐎涙鍚€閻樿埖鈧?- 0:闂呮劘妫?缁備胶鏁? 1:閺勫墽銇?閸氼垳鏁?,
    sys_dict_in_system int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀崘鍛枂 - 0:闂堢偛鍞寸純?閸欘垰鍨归梽?, 1:閸愬懐鐤?娑撳秴褰查崚鐘绘珟)',
    sys_dict_i18n varchar(255)   NULL DEFAULT NULL COMMENT '閸ヤ粙妾崠鏍ㄧ垼鐠?- 閻劋绨径姘愁嚔鐟封偓閺€顖涘瘮閻ㄥ埇18n闁?,
    sys_dict_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 鐎涙鍚€閸︺劌鍨悰銊よ厬閻ㄥ嫭妯夌粈娲€庢惔?,
    sys_dict_remark varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€閹诲繗鍫?- 鐎涙鍚€閻劑鈧棁顕╅弰?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_dict_id) USING BTREE,
    UNIQUE INDEX uk_dict_code (sys_dict_code) USING BTREE COMMENT '鐎涙鍚€缂傛牜鐖滈崬顖欑缁便垹绱?,
    INDEX idx_dict_pid (sys_dict_pid) USING BTREE COMMENT '閻栬泛鐡ч崗鍝ュ偍瀵?,
    INDEX idx_dict_status (sys_dict_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '鐎涙鍚€鐞?- 缁崵绮洪弫鐗堝祦鐎涙鍚€閿涘瞼顓搁悶鍡楁倗缁粯鐏囨稉鎯р偓? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 鐎涙鍚€鐎涙劙銆嶇悰?(sys_dict_item)
-- 閻劑鈧? 鐎涙鍚€閻ㄥ嫬鍙挎担鎾烩偓澶愩€嶉崐?-- =====================================================
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    sys_dict_item_id int NOT NULL AUTO_INCREMENT COMMENT '鐎涙鍚€妞ょD - 娑撳鏁懛顏勵杻',
    sys_dict_id int NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偛鐡ч崗绐盌 - 閸忓疇浠坰ys_dict鐞?,
    sys_dict_item_name varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€妞ょ懓鎮曠粔?- 闁銆嶉惃鍕▔缁€鐑樻瀮閺?,
    sys_dict_item_code varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€妞ゅ湱绱惍?- 闁銆嶉惃鍕偓?娴狅絿鐖?,
    sys_dict_item_icon varchar(255)   NULL DEFAULT NULL COMMENT '閸ョ偓鐖?- 闁銆嶉惃鍕禈閺嶅洨琚崥宥嗗灗URL',
    sys_dict_item_status int NULL DEFAULT 1 COMMENT '鐎涙鍚€妞ゅ湱濮搁幀?- 0:闂呮劘妫?缁備胶鏁? 1:閺勫墽銇?閸氼垳鏁?,
    sys_dict_item_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 闁銆嶉崷銊ュ灙鐞涖劋鑵戦惃鍕▔缁€娲€庢惔?,
    sys_dict_item_i18n varchar(255)   NULL DEFAULT NULL COMMENT '閸ヤ粙妾崠鏍ㄧ垼鐠?- 閻劋绨径姘愁嚔鐟封偓閺€顖涘瘮閻ㄥ埇18n闁?,
    sys_dict_item_delete int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀崚鐘绘珟 - 0:閺堫亜鍨归梽? 1:瀹告彃鍨归梽?鏉烆垰鍨归梽?',
    sys_dict_item_property varchar(255)   NULL DEFAULT NULL COMMENT '閸欘垶鍘ょ純顕€銆?- 鐠囥儱鐡ч崗鎼併€嶉弨顖涘瘮閻ㄥ嫭澧跨仦鏇㈠帳缂冾喖鐡у▓闈涘灙鐞?,
    sys_dict_item_remark varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙鍚€妞よ寮挎潻?- 闁銆嶉悽銊┾偓鏃囶嚛閺?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_dict_item_id) USING BTREE,
    INDEX idx_dict_id (sys_dict_id) USING BTREE COMMENT '鐎涙鍚€ID缁便垹绱?,
    INDEX idx_item_code (sys_dict_item_code) USING BTREE COMMENT '鐎涙鍚€妞ゅ湱绱惍浣哄偍瀵?,
    INDEX idx_item_status (sys_dict_item_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '鐎涙鍚€鐎涙劙銆嶇悰?- 鐎涙鍚€閻ㄥ嫬鍙挎担鎾烩偓澶愩€嶉崐? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 鐎涙鍚€闁板秶鐤嗘い纭呫€?(sys_dict_item_property)
-- 閻劑鈧? 鐎涙鍚€妞ゅ湱娈戦幍鈺佺潔鐏炵偞鈧囧帳缂?-- =====================================================
DROP TABLE IF EXISTS sys_dict_item_property;
CREATE TABLE sys_dict_item_property (
    sys_dict_item_property_id int NOT NULL AUTO_INCREMENT COMMENT '闁板秶鐤嗘い绗紻 - 娑撳鏁懛顏勵杻',
    sys_dict_item_id int NULL DEFAULT NULL COMMENT '鐎涙鍚€妞ょD - 閸忓疇浠坰ys_dict_item鐞?,
    sys_dict_item_property_group varchar(20)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗘い鐟板瀻缂?- 鐏炵偞鈧勫鐏炵偟娈戞稉姘閸掑棛绮?,
    sys_dict_item_property_name varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗘い鐟版倳缁?- 鐏炵偞鈧呮畱闁款喖鎮?,
    sys_dict_item_property_value varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗘い鐟扳偓?- 鐏炵偞鈧呮畱閸?,
    sys_dict_item_property_selected_type varchar(255)   NULL DEFAULT NULL COMMENT '閸婅偐琚崹?- Boolean/String/Number缁?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_dict_item_property_id) USING BTREE,
    INDEX idx_dict_item_id (sys_dict_item_id) USING BTREE COMMENT '鐎涙鍚€妞ょD缁便垹绱?,
    INDEX idx_property_group (sys_dict_item_property_group) USING BTREE COMMENT '閸掑棛绮嶇槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '鐎涙鍚€闁板秶鐤嗘い纭呫€?- 鐎涙鍚€妞ゅ湱娈戦幍鈺佺潔鐏炵偞鈧囧帳缂? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 缁崵绮洪幙宥勭稊閺冦儱绻旂悰?(sys_log)
-- 閻劑鈧? 鐠佹澘缍嶇化鑽ょ埠閹垮秳缍旈弮銉ョ箶閿涘瞼鏁ゆ禍搴☆吀鐠佲€虫嫲闂傤噣顣芥潻鍊熼嚋
-- =====================================================
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '閺冦儱绻擨D - 娑撳鏁懛顏勵杻',
    sys_log_username varchar(255)   NULL DEFAULT NULL COMMENT '閹垮秳缍旂拹锕€褰?- 閹笛嗩攽閹垮秳缍旈惃鍕暏閹寸柉澶勯崣?,
    sys_log_address varchar(255)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪癐P - 鐎广垺鍩涚粩鐤楶閸︽澘娼?,
    sys_log_url varchar(255)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崷鏉挎絻 - API閹恒儱褰涚捄顖氱窞',
    sys_log_param varchar(255)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崣鍌涙殶 - 閹恒儱褰涚拠閿嬬湴閸欏倹鏆?閼磋鲸鏅遍崥?',
    sys_log_from varchar(255)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪板Ο鈥虫健 - 閹碘偓鐏炵偛濮涢懗鑺ツ侀崸妤€鎮曠粔?,
    sys_log_status int NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴悩鑸碘偓?- 0:婢惰精瑙? 1:閹存劕濮?,
    sys_log_cost int NULL DEFAULT NULL COMMENT '閼版妞?- 鐠囬攱鐪版径鍕倞閼版妞?濮ｎ偆顫?',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '閻劍鍩涢崥宥囧偍瀵?,
    INDEX idx_status (sys_log_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?,
    INDEX idx_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?,
    INDEX idx_url (sys_log_url) USING BTREE COMMENT '鐠囬攱鐪伴崷鏉挎絻缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '缁崵绮洪幙宥勭稊閺冦儱绻旂悰?- 鐠佹澘缍岮PI鐠囬攱鐪伴弮銉ョ箶閻劋绨€孤ゎ吀' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 缁崵绮洪柊宥囩枂閸掑棛绮嶇悰?(sys_setting_group)
-- 閻劑鈧? 鐎靛湱閮寸紒鐔煎帳缂冾噣銆嶆潻娑滎攽閸掑棛绮嶇粻锛勬倞
-- =====================================================
DROP TABLE IF EXISTS sys_setting_group;
CREATE TABLE sys_setting_group (
    sys_setting_group_id int NOT NULL AUTO_INCREMENT COMMENT '閸掑棛绮岻D - 娑撳鏁懛顏勵杻',
    sys_setting_group_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉崥宥囆?- 闁板秶鐤嗛崚鍡欑矋閻ㄥ嫭妯夌粈鍝勬倳缁?,
    sys_setting_group_code varchar(255)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶇紓鏍垳 - 闁板秶鐤嗛崚鍡欑矋閻ㄥ嫬鏁稉鈧弽鍥槕閻?,
    sys_setting_group_icon varchar(255)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉崶鐐垼 - 閸ョ偓鐖ｇ猾璇叉倳閹存湧RL',
    sys_setting_group_enable tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏 - 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_setting_group_remark varchar(255)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉幓蹇氬牚 - 閸掑棛绮嶉悽銊┾偓鏃囶嚛閺?,
    sys_setting_group_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 閸掑棛绮嶉崷銊ュ灙鐞涖劋鑵戦惃鍕▔缁€娲€庢惔?,
    sys_setting_group_use_project_interface tinyint NULL DEFAULT 0 COMMENT '閺勵垰鎯佹担璺ㄦ暏妞ゅ湱娲扮紒鍕复閸?- 0:閸? 1:閺?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_setting_group_id) USING BTREE,
    UNIQUE INDEX uk_group_code (sys_setting_group_code) USING BTREE COMMENT '閸掑棛绮嶇紓鏍垳閸烆垯绔寸槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '缁崵绮洪柊宥囩枂閸掑棛绮嶇悰?- 鐎靛湱閮寸紒鐔煎帳缂冾噣銆嶆潻娑滎攽閸掑棛绮嶇粻锛勬倞' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 缁崵绮洪崗銊ョ湰闁板秶鐤嗙悰?(sys_setting)
-- 閻劑鈧? 鐎涙ê鍋嶇化鑽ょ埠閸忋劌鐪柊宥囩枂閸欏倹鏆?-- =====================================================
DROP TABLE IF EXISTS sys_setting;
CREATE TABLE sys_setting (
    sys_setting_id int NOT NULL AUTO_INCREMENT COMMENT '闁板秶鐤咺D - 娑撳鏁懛顏勵杻',
    sys_setting_name varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗛崥宥囆?- 闁板秶鐤嗘い鍦畱闁款喖鎮?,
    sys_setting_code varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗙紓鏍垳 - 閸烆垯绔撮弽鍥槕閻緤绱欐俊?login.title閿?,
    sys_setting_value text NULL DEFAULT NULL COMMENT '闁板秶鐤嗛崐?- 闁板秶鐤嗘い鍦畱閸?,
    sys_setting_value_type varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗙猾璇茬€?- Boolean/String/Number/Color/Dict缁涘绱濋悽銊ょ艾閸撳秶顏紒鍕閸掋倖鏌?,
    sys_setting_group varchar(255)   NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偛鍨庣紒?- 闁板秶鐤嗘い瑙勫鐏炵偟娈戦崚鍡欑矋缂傛牜鐖?,
    sys_setting_status int NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏 - 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_setting_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 闁板秶鐤嗘い鐟版躬閸掑棛绮嶆稉顓犳畱閺勫墽銇氭い鍝勭碍',
    sys_setting_collect int NULL DEFAULT 0 COMMENT '閺勵垰鎯佹径姘偓濂告肠閸?- 0:閸楁洑閲滈崐? 1:婢舵矮閲滈崐鑲╂畱闂嗗棗鎮?,
    sys_setting_in_system int NULL DEFAULT 0 COMMENT '閺勵垰鎯佺化鑽ょ埠闁板秶鐤?- 0:闂堢偟閮寸紒鐔煎帳缂? 1:缁崵绮洪崘鍛枂闁板秶鐤?,
    sys_setting_app_inner int NULL DEFAULT 0 COMMENT '閺勵垰鎯佹惔鏃傛暏閸氼垰濮╅柊宥囩枂 - 0:閸? 1:閺?瑜板崬鎼锋惔鏃傛暏閸氼垰濮?',
    sys_setting_admin int NULL DEFAULT 0 COMMENT '閺勵垰鎯佺粻锛勬倞閸涙ü绗撶仦?- 0:閹碘偓閺堝姹夐崣顖濐潌, 1:娴犲懐顓搁悶鍡楁喅閸欘垵顫?,
    sys_setting_version int NULL DEFAULT 1 COMMENT '闁板秶鐤嗛悧鍫熸拱 - 閻劋绨柊宥囩枂閸欐ɑ娲挎潻鍊熼嚋',
    sys_setting_config varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗛崣鍌涙殶 - 闂勫嫬濮為惃鍕帳缂冾喖寮弫?婵″倸鍙ч懕鏂跨摟閸忕D)',
    sys_setting_remark varchar(255)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗘径鍥ㄦ暈 - 闁板秶鐤嗘い鍦暏闁棁顕╅弰?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_setting_id) USING BTREE,
    UNIQUE INDEX uk_setting_name_group (sys_setting_name, sys_setting_group) USING BTREE COMMENT '闁板秶鐤嗛崥宥呮嫲閸掑棛绮嶉懕鏂挎値閸烆垯绔寸槐銏犵穿',
    UNIQUE INDEX uk_setting_code (sys_setting_code) USING BTREE COMMENT '闁板秶鐤嗙紓鏍垳閸烆垯绔寸槐銏犵穿',
    INDEX idx_setting_group (sys_setting_group) USING BTREE COMMENT '閸掑棛绮嶇槐銏犵穿',
    INDEX idx_setting_status (sys_setting_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '缁崵绮洪崗銊ョ湰闁板秶鐤嗙悰?- 鐎涙ê鍋嶇化鑽ょ埠閸忋劌鐪柊宥囩枂閸欏倹鏆? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 闂勬劖绁﹂柊宥囩枂鐞?(sys_limit_configuration)
-- 閻劑鈧? 闁板秶鐤咥PI閹恒儱褰涢惃鍕濞翠浇顫夐崚?-- =====================================================
DROP TABLE IF EXISTS sys_limit_configuration;
CREATE TABLE sys_limit_configuration (
    sys_limit_configuration_id bigint NOT NULL AUTO_INCREMENT COMMENT '闁板秶鐤咺D - 娑撳鏁懛顏勵杻',
    sys_limit_name varchar(255)   NULL DEFAULT NULL COMMENT '闂勬劖绁︾憴鍕灟閸氬秶袨 - 鐟欏嫬鍨惃鍕▔缁€鍝勬倳缁?,
    sys_limit_path varchar(500)   NULL DEFAULT NULL COMMENT '閹恒儱褰涚捄顖氱窞 - 闂団偓鐟曚線妾哄ù浣烘畱API鐠侯垰绶為敍灞炬暜閹镐竸nt妞嬪孩鐗搁崠褰掑帳',
    sys_limit_for_period int NULL DEFAULT 10 COMMENT '閸涖劍婀＄拋绋垮讲閺?- 濮ｅ繋閲滈崚閿嬫煀閸涖劍婀￠崘鍛帒鐠佸摜娈戦張鈧径褑顕Ч鍌涙殶',
    sys_limit_refresh_period_seconds bigint NULL DEFAULT 1 COMMENT '閸掗攱鏌婇崨銊︽埂 - 闂勬劖绁︾拋鈩冩殶閸ｃ劎娈戦崚閿嬫煀閸涖劍婀?缁?',
    sys_limit_timeout_duration_millis bigint NULL DEFAULT 0 COMMENT '鐡掑懏妞傞弮鍫曟？ - 閼惧嘲褰囩拋绋垮讲閻ㄥ嫯绉撮弮鑸垫闂?濮ｎ偆顫?閿?鐞涖劎銇氱粩瀣祮鏉╂柨娲?,
    sys_limit_dimension varchar(50)   NULL DEFAULT 'GLOBAL' COMMENT '闂勬劖绁︾紒鏉戝 - GLOBAL:閸忋劌鐪? IP:閹稿P, USER:閹稿鏁ら幋? API:閹稿甯撮崣?,
    sys_limit_key_expression varchar(500)   NULL DEFAULT NULL COMMENT '閼奉亜鐣炬稊澶愭暛鐞涖劏鎻?- SpEL鐞涖劏鎻蹇ョ礉閻劋绨幓鎰絿闂勬劖绁﹂柨?,
    sys_limit_fallback_method varchar(255)   NULL DEFAULT NULL COMMENT '闂勫秶楠囬弬瑙勭《 - 闂勬劖绁︾憴锕€褰傞崥搴ｆ畱闂勫秶楠囨径鍕倞閺傝纭堕崥?,
    sys_limit_message varchar(500)   NULL DEFAULT '鐠囬攱鐪版潻鍥︾艾妫版垹绠掗敍宀冾嚞缁嬪秴鎮楅崘宥堢槸' COMMENT '闁挎瑨顕ゅ☉鍫熶紖 - 闂勬劖绁︾憴锕€褰傞弮鎯扮箲閸ョ偟绮扮€广垺鍩涚粩顖滄畱閹绘劗銇氭穱鈩冧紖',
    sys_limit_status int NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏 - 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_limit_description varchar(500)   NULL DEFAULT NULL COMMENT '鐟欏嫬鍨幓蹇氬牚 - 闂勬劖绁︾憴鍕灟閻ㄥ嫯顕涚紒鍡氼嚛閺?,
    sys_limit_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 鐟欏嫬鍨导妯哄帥缁狙嶇礉閺佹澘鐡х搾濠傜毈娴兼ê鍘涚痪褑绉烘?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_limit_configuration_id) USING BTREE,
    INDEX idx_limit_path (sys_limit_path(255)) USING BTREE COMMENT '閹恒儱褰涚捄顖氱窞缁便垹绱?,
    INDEX idx_limit_status (sys_limit_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?,
    INDEX idx_limit_dimension (sys_limit_dimension) USING BTREE COMMENT '闂勬劖绁︾紒鏉戝缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '闂勬劖绁﹂柊宥囩枂鐞?- 闁板秶鐤咥PI閹恒儱褰涢惃鍕濞翠浇顫夐崚? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 闂勬劖绁︾拋鏉跨秿鐞?(sys_limit_record)
-- 閻劑鈧? 鐠佹澘缍嶉梽鎰ウ鐟欙箑褰傞惃鍕嚊缂佸棔淇婇幁?-- =====================================================
DROP TABLE IF EXISTS sys_limit_record;
CREATE TABLE sys_limit_record (
    sys_limit_record_id bigint NOT NULL AUTO_INCREMENT COMMENT '鐠佹澘缍岻D - 娑撳鏁懛顏勵杻',
    sys_limit_configuration_id bigint NULL DEFAULT NULL COMMENT '闂勬劖绁﹂柊宥囩枂ID - 閸忓疇浠坰ys_limit_configuration鐞?,
    sys_limit_path varchar(500)   NULL DEFAULT NULL COMMENT '閹恒儱褰涚捄顖氱窞 - 鐞氼偊妾哄ù浣烘畱API鐠侯垰绶?,
    sys_limit_name varchar(255)   NULL DEFAULT NULL COMMENT '闂勬劖绁︾憴鍕灟閸氬秶袨',
    sys_limit_dimension varchar(50)   NULL DEFAULT NULL COMMENT '闂勬劖绁︾紒鏉戝 - GLOBAL/IP/USER/API',
    sys_limit_key varchar(255)   NULL DEFAULT NULL COMMENT '闂勬劖绁﹂柨顔尖偓?- 閸忚渹缍嬮惃鍕濞翠焦鐖ｇ拠?婵′痉P閸︽澘娼冮妴浣烘暏閹寸īD)',
    sys_limit_time datetime NULL DEFAULT NULL COMMENT '闂勬劖绁﹂弮鍫曟？ - 鐟欙箑褰傞梽鎰ウ閻ㄥ嫭妞傞梻?,
    sys_user_id bigint NULL DEFAULT NULL COMMENT '閻劍鍩汭D - 鐞氼偊妾哄ù浣烘畱閻劍鍩汭D',
    sys_user_name varchar(255)   NULL DEFAULT NULL COMMENT '閻劍鍩涢崥?- 鐞氼偊妾哄ù浣烘畱閻劍鍩涢崥?,
    client_ip varchar(50)   NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩鐤楶 - 鐠囬攱鐪伴惃鍕吂閹撮顏琁P閸︽澘娼?,
    request_url varchar(500)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪癠RL - 鐎瑰本鏆ｉ惃鍕嚞濮逛繃RL',
    request_method varchar(10)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴弬瑙勭《 - GET/POST/PUT/DELETE缁?,
    user_agent varchar(500)   NULL DEFAULT NULL COMMENT 'User-Agent - 鐎广垺鍩涚粩顖涚セ鐟欏牆娅扷A娣団剝浼?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_limit_record_id) USING BTREE,
    INDEX idx_configuration_id (sys_limit_configuration_id) USING BTREE COMMENT '闁板秶鐤咺D缁便垹绱?,
    INDEX idx_limit_time (sys_limit_time) USING BTREE COMMENT '闂勬劖绁﹂弮鍫曟？缁便垹绱?,
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '閻劍鍩汭D缁便垹绱?,
    INDEX idx_client_ip (client_ip) USING BTREE COMMENT '鐎广垺鍩涚粩鐤楶缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '闂勬劖绁︾拋鏉跨秿鐞?- 鐠佹澘缍嶉梽鎰ウ鐟欙箑褰傞惃鍕嚊缂佸棔淇婇幁? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 閼哄倸浜ｉ弮銉ㄣ€?(sys_holiday)
-- 閻劑鈧? 鐎涙ê鍋嶅▔鏇炵暰閼哄倸浜ｉ弮銉ユ嫲鐠嬪啩绱ゅ銉ょ稊閺冦儰淇婇幁?-- =====================================================
DROP TABLE IF EXISTS sys_holiday;
CREATE TABLE sys_holiday (
    sys_holiday_id int NOT NULL AUTO_INCREMENT COMMENT '閼哄倸浜ｉ弮顧廌 - 娑撳鏁懛顏勵杻',
    sys_holiday_year int NULL DEFAULT NULL COMMENT '閼哄倸浜ｉ弮銉ュ嬀娴?- 婵?024閵?025',
    sys_holiday_date date NULL DEFAULT NULL COMMENT '閼哄倸浜ｉ弮銉︽）閺?- 閸忚渹缍嬮弮銉︽埂',
    sys_holiday_name varchar(100)   NULL DEFAULT NULL COMMENT '閼哄倸浜ｉ弮銉ユ倳缁?- 婵″倹妲懞鍌樷偓浣告禇鎼村棜濡妴浣界殶娴?,
    sys_holiday_is_holiday tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀懞鍌氫海閺?- 1:閼哄倸浜ｉ弮銉ょ搐閹? 0:鐠嬪啩绱ゅ銉ょ稊閺?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    PRIMARY KEY (sys_holiday_id) USING BTREE,
    UNIQUE INDEX uk_date (sys_holiday_date) USING BTREE COMMENT '閺冦儲婀￠崬顖欑缁便垹绱?- 绾喕绻氬В蹇庨嚋閺冦儲婀￠崣顏呮箒娑撯偓閺壜ゎ唶瑜?,
    INDEX idx_year (sys_holiday_year) USING BTREE COMMENT '楠炵繝鍞ょ槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '閼哄倸浜ｉ弮銉ㄣ€?- 鐎涙ê鍋嶅▔鏇炵暰閼哄倸浜ｉ弮銉ユ嫲鐠嬪啩绱ゅ銉ょ稊閺? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 閺傚洣娆㈢化鑽ょ埠閸掑棛绮嶇悰?(sys_file_system_group)
-- 閻劑鈧? 鐎甸€涚瑐娴肩姷娈戦弬鍥︽鏉╂稖顢戦崚鍡欑矋缁狅紕鎮?-- =====================================================
DROP TABLE IF EXISTS sys_file_system_group;
CREATE TABLE sys_file_system_group (
    sys_file_system_group_id int NOT NULL AUTO_INCREMENT COMMENT '閸掑棛绮岻D - 娑撳鏁懛顏勵杻',
    sys_file_system_group_name varchar(100)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉崥宥囆?- 閺傚洣娆㈤崚鍡欑矋閻ㄥ嫭妯夌粈鍝勬倳缁?,
    sys_file_system_group_parent_id int NULL DEFAULT 0 COMMENT '閻栬泛鍨庣紒鍑 - 閺€顖涘瘮婢舵氨楠囬崚鍡欑矋閿?鐞涖劎銇氭い鍓侀獓閸掑棛绮?,
    sys_file_system_group_path varchar(255)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶇捄顖氱窞 - 閸掑棛绮嶉惃鍕叚鐠侯垰绶為弽鍥槕',
    sys_file_system_group_full_path varchar(500)   NULL DEFAULT NULL COMMENT '鐎瑰本鏆ｇ捄顖氱窞 - 娴犲孩鐗撮崚鏉跨秼閸撳秴鍨庣紒鍕畱鐎瑰本鏆ｇ捄顖氱窞',
    sys_file_system_group_level int NULL DEFAULT 1 COMMENT '閸掑棛绮嶇仦鍌滈獓 - 閸掑棛绮嶉崷銊︾埐娑擃厾娈戠仦鍌滈獓濞ｅ崬瀹?,
    sys_file_system_group_tree_path varchar(500)   NULL DEFAULT NULL COMMENT '閺嶆垼鐭惧?- 閸掑棛绮岻D缂佸嫭鍨氶惃鍕埐鐠侯垰绶為敍宀€鏁ゆ禍搴℃彥闁喐鐓＄拠?,
    sys_file_system_group_description varchar(500)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉幓蹇氬牚 - 閸掑棛绮嶉悽銊┾偓鏃囶嚛閺?,
    sys_file_system_group_is_default tinyint NULL DEFAULT 0 COMMENT '閺勵垰鎯佹妯款吇閸掑棛绮?- 0:閸? 1:閺?閺傜増鏋冩禒鍫曠帛鐠併倕缍婇崗?',
    sys_file_system_group_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨崣?- 閸掑棛绮嶉崷銊ュ灙鐞涖劋鑵戦惃鍕▔缁€娲€庢惔?,
    sys_file_system_group_status int NULL DEFAULT 1 COMMENT '閸掑棛绮嶉悩鑸碘偓?- 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_file_system_group_icon varchar(100)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶉崶鐐垼 - 閸ョ偓鐖ｇ猾璇叉倳',
    sys_file_system_group_color varchar(20)   NULL DEFAULT NULL COMMENT '閸掑棛绮嶆０婊嗗 - 閸椾礁鍙氭潻娑樺煑妫版粏澹婇崐?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_file_system_group_id) USING BTREE,
    INDEX idx_parent_id (sys_file_system_group_parent_id) USING BTREE COMMENT '閻栬泛鍨庣紒鍕偍瀵?,
    INDEX idx_status (sys_file_system_group_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '閺傚洣娆㈢化鑽ょ埠閸掑棛绮嶇悰?- 鐎甸€涚瑐娴肩姷娈戦弬鍥︽鏉╂稖顢戦崚鍡欑矋缁狅紕鎮? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 16. 閺傚洣娆㈢化鑽ょ埠闁板秶鐤嗙悰?(sys_file_system_setting)
-- 閻劑鈧? 闁板秶鐤嗛弬鍥︽娑撳﹣绱堕崪灞界摠閸屻劎娈戦崣鍌涙殶
-- =====================================================
DROP TABLE IF EXISTS sys_file_system_setting;
CREATE TABLE sys_file_system_setting (
    sys_file_system_setting_id int NOT NULL AUTO_INCREMENT COMMENT '闁板秶鐤咺D - 娑撳鏁懛顏勵杻',
    sys_file_system_setting_name varchar(100)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗛崥宥囆?- 闁板秶鐤嗛弬瑙勵攳閻ㄥ嫬鎮曠粔?,
    sys_file_system_setting_chunk_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏閸掑棛澧栨稉濠佺炊 - 0:閸? 1:閺?,
    sys_file_system_setting_chunk_size_mb int NULL DEFAULT 100 COMMENT '閸掑棛澧栨径褍鐨?- 濮ｅ繋閲滈崚鍡欏閻ㄥ嫬銇囩亸?MB)',
    sys_file_system_setting_auto_merge_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀懛顏勫З閸氬牆鑻?- 0:閸? 1:閺?閸掑棛澧栨稉濠佺炊鐎瑰本鍨氶崥搴ゅ殰閸斻劌鎮庨獮?',
    sys_file_system_setting_merge_task_limit int NULL DEFAULT 10 COMMENT '閸氬牆鑻熸禒璇插闂勬劕鍩?- 閸氬本妞傛潻娑滎攽閻ㄥ嫬鎮庨獮鏈垫崲閸斺剝娓舵径褎鏆熼柌?,
    sys_file_system_setting_manual_merge_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀弨顖涘瘮閹靛濮╅崥鍫濊嫙 - 0:閸? 1:閺?,
    sys_file_system_setting_http_server_enabled tinyint NULL DEFAULT 0 COMMENT '閺勵垰鎯佸鈧崥鐤昑TP閺堝秴濮?- 0:閸? 1:閺?,
    sys_file_system_setting_http_server_port int NULL DEFAULT 8888 COMMENT 'HTTP閺堝秴濮熺粩顖氬經',
    sys_file_system_setting_http_server_host varchar(100)   NULL DEFAULT '0.0.0.0' COMMENT 'HTTP閺堝秴濮熸稉缁樻簚',
    sys_file_system_setting_http_server_context varchar(100)   NULL DEFAULT '/file' COMMENT 'HTTP娑撳﹣绗呴弬鍥熅瀵?,
    sys_file_system_setting_download_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏娑撳娴?- 0:閸? 1:閺?,
    sys_file_system_setting_preview_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏妫板嫯顫?- 0:閸? 1:閺?,
    sys_file_system_setting_http_access_domain varchar(255)   NULL DEFAULT NULL COMMENT 'HTTP鐠佸潡妫堕崺鐔锋倳 - 閺傚洣娆㈢拋鍧楁６閻ㄥ嫬鐓欓崥宥呭缂傗偓',
    sys_file_system_setting_allowed_types varchar(1000)   NULL DEFAULT NULL COMMENT '閸忎浇顔忛惃鍕瀮娴犲墎琚崹?- 閺傚洣娆㈤幍鈺佺潔閸氬秶娅ч崥宥呭礋閿涘矂鈧褰块崚鍡涙',
    sys_file_system_setting_max_file_size_mb int NULL DEFAULT 1024 COMMENT '閸楁洘鏋冩禒鑸垫付婢堆冦亣鐏?- 閸楁洑閲滈弬鍥︽閻ㄥ嫭娓舵径褍銇囩亸?MB)',
    sys_file_system_setting_storage_root_path varchar(500)   NULL DEFAULT NULL COMMENT '鐎涙ê鍋嶉弽纭呯熅瀵?- 閺傚洣娆㈢€涙ê鍋嶉惃鍕壌閻╊喖缍嶇捄顖氱窞',
    sys_file_system_setting_temp_path varchar(500)   NULL DEFAULT NULL COMMENT '娑撳瓨妞傞弬鍥︽鐠侯垰绶?- 閸掑棛澧栨稉濠佺炊閻ㄥ嫪澶嶉弮璺虹摠閸屻劎娲拌ぐ?,
    sys_file_system_setting_retention_days int NULL DEFAULT 0 COMMENT '閺傚洣娆㈡穱婵堟殌婢垛晜鏆?- 0鐞涖劎銇氬闀愮畽娣囨繄鏆€',
    sys_file_system_setting_enabled tinyint NULL DEFAULT 1 COMMENT '閺勵垰鎯侀崥顖滄暏 - 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_file_system_setting_default_storage_type varchar(50)   NULL DEFAULT 'local' COMMENT '姒涙顓荤€涙ê鍋嶇猾璇茬€?- local:閺堫剙婀? oss:闂冨潡鍣锋禍鎱淪S, cos:閼垫崘顔嗘禍鎱峅S缁?,
    sys_file_system_setting_remark varchar(500)   NULL DEFAULT NULL COMMENT '闁板秶鐤嗘径鍥ㄦ暈',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_file_system_setting_id) USING BTREE,
    INDEX idx_enabled (sys_file_system_setting_enabled) USING BTREE COMMENT '閸氼垳鏁ら悩鑸碘偓浣哄偍瀵?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '閺傚洣娆㈢化鑽ょ埠闁板秶鐤嗙悰?- 闁板秶鐤嗛弬鍥︽娑撳﹣绱堕崪灞界摠閸屻劎娈戦崣鍌涙殶' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 17. 閺傚洣娆㈢化鑽ょ埠娑撴槒銆?(sys_file_system)
-- 閻劑鈧? 鐎涙ê鍋嶆稉濠佺炊閺傚洣娆㈤惃鍕帗閺佺増宓佹穱鈩冧紖
-- =====================================================
DROP TABLE IF EXISTS sys_file_system;
CREATE TABLE sys_file_system (
    sys_file_system_id int NOT NULL AUTO_INCREMENT COMMENT '閺傚洣娆D - 娑撳鏁懛顏勵杻',
    sys_file_system_tid varchar(64)   NULL DEFAULT NULL COMMENT '娴犺濮熼崬顖欑閺嶅洩鐦?- 娑撳﹣绱舵禒璇插閻ㄥ嫬鏁稉鈧琁D閿涘瞼鏁ゆ禍搴㈡焽閻愬湱鐢绘导?,
    sys_file_system_bucket varchar(255)   NULL DEFAULT NULL COMMENT '鐎涙ê鍋嶅?- 鐎电钖勭€涙ê鍋嶉惃鍕€婇崥宥囆?,
    sys_file_system_name varchar(255)   NULL DEFAULT NULL COMMENT '閺傚洣娆㈤崥宥囆?- 鐎涙ê鍋嶉崥搴ｆ畱閺傚洣娆㈤崥?闁艾鐖堕弰鐤稶ID)',
    sys_file_system_original_name varchar(255)   NULL DEFAULT NULL COMMENT '閸樼喎顫愰弬鍥︽閸?- 閻劍鍩涙稉濠佺炊閺冨墎娈戦崢鐔奉潗閺傚洣娆㈤崥?,
    sys_file_system_type varchar(50)   NULL DEFAULT NULL COMMENT '鐎涙ê鍋嶇猾璇茬€?- local/oss/cos/minio缁?,
    sys_file_system_extension varchar(20)   NULL DEFAULT NULL COMMENT '閺傚洣娆㈤幍鈺佺潔閸?- 婵′坎pg閵嗕垢df閵嗕龚ocx',
    sys_file_system_path varchar(500)   NULL DEFAULT NULL COMMENT '閺傚洣娆㈢捄顖氱窞 - 閺傚洣娆㈤崷銊ョ摠閸屻劋鑵戦惃鍕祲鐎电鐭惧?,
    sys_file_system_size bigint NULL DEFAULT NULL COMMENT '閺傚洣娆㈡径褍鐨?- 閺傚洣娆㈡径褍鐨?鐎涙濡?',
    sys_file_system_md5 varchar(32)   NULL DEFAULT NULL COMMENT '閺傚洣娆D5 - 閺傚洣娆㈤惃鍑狣5閸濆牆绗囬崐纭风礉閻劋绨粔鎺嶇炊閸滃苯鐣弫瀛樷偓褎鐗庢?,
    sys_file_system_chunk_total int NULL DEFAULT NULL COMMENT '閸掑棛澧栭幀缁樻殶 - 閸掑棛澧栨稉濠佺炊閺冨墎娈戦幀璇插瀻閻楀洦鏆?,
    sys_file_system_chunk_uploaded int NULL DEFAULT 0 COMMENT '瀹歌弓绗傛导鐘插瀻閻楀洦鏆?- 瑜版挸澧犲鎻掔暚閹存劒绗傛导鐘垫畱閸掑棛澧栭弫浼村櫤',
    sys_file_system_chunk_size bigint NULL DEFAULT NULL COMMENT '閸掑棛澧栨径褍鐨?- 濮ｅ繋閲滈崚鍡欏閻ㄥ嫬銇囩亸?鐎涙濡?',
    sys_file_system_version int NULL DEFAULT 0 COMMENT '閻楀牊婀伴崣?- 閺傚洣娆㈤悧鍫熸拱閿涘瞼鏁ゆ禍搴濈鐟欏倿鏀?,
    sys_file_system_status int NULL DEFAULT 0 COMMENT '閺傚洣娆㈤悩鑸碘偓?- 0:娑撳﹣绱舵稉? 1:娑撳﹣绱剁€瑰本鍨? 2:閸氬牆鑻熸稉? 3:瀹告彃鐣幋? -1:娑撳﹣绱舵径杈Е',
    sys_file_system_http_enabled tinyint NULL DEFAULT 0 COMMENT '閺勵垰鎯侀崥顖滄暏HTTP鐠佸潡妫?- 0:閸? 1:閺?,
    sys_file_system_http_url varchar(500)   NULL DEFAULT NULL COMMENT 'HTTP鐠佸潡妫禪RL - 閺傚洣娆㈤惃鍕暚閺佺顔栭梻顔兼勾閸р偓',
    sys_file_system_service_id int NULL DEFAULT NULL COMMENT '缂佹垵鐣鹃張宥呭ID - 閸忓疇浠堥惃鍕瑹閸斺剝婀囬崝顡廌',
    sys_file_system_group_id int NULL DEFAULT NULL COMMENT '閸掑棛绮岻D - 閸忓疇浠坰ys_file_system_group鐞?,
    sys_file_system_upload_start_time datetime NULL DEFAULT NULL COMMENT '娑撳﹣绱跺鈧慨瀣闂?,
    sys_file_system_upload_complete_time datetime NULL DEFAULT NULL COMMENT '娑撳﹣绱剁€瑰本鍨氶弮鍫曟？',
    sys_file_system_local_path varchar(500)   NULL DEFAULT NULL COMMENT '閺堫剙婀存稉瀛樻鐠侯垰绶?- 閸掑棛澧栨稉濠佺炊閺冨墎娈戞稉瀛樻鐎涙ê鍋嶇捄顖氱窞',
    sys_file_system_summary varchar(500)   NULL DEFAULT NULL COMMENT '閺傚洣娆㈤幓蹇氬牚 - 閺傚洣娆㈤惃鍕槵濞夈劏顕╅弰?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_file_system_id) USING BTREE,
    UNIQUE INDEX uk_tid (sys_file_system_tid) USING BTREE COMMENT '娴犺濮烮D閸烆垯绔寸槐銏犵穿',
    INDEX idx_status (sys_file_system_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?,
    INDEX idx_group_id (sys_file_system_group_id) USING BTREE COMMENT '閸掑棛绮岻D缁便垹绱?,
    INDEX idx_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?,
    INDEX idx_md5 (sys_file_system_md5) USING BTREE COMMENT 'MD5缁便垹绱?- 閻劋绨粔鎺嶇炊閺屻儴顕?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '閺傚洣娆㈢化鑽ょ埠娑撴槒銆?- 鐎涙ê鍋嶆稉濠佺炊閺傚洣娆㈤惃鍕帗閺佺増宓佹穱鈩冧紖' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 18. 閺傚洣娆㈢化鑽ょ埠閸掑棛澧栫悰?(sys_file_system_part)
-- 閻劑鈧? 鐎涙ê鍋嶉崚鍡欏娑撳﹣绱堕惃鍕槨娑擃亜鍨庨悧鍥︿繆閹?-- =====================================================
DROP TABLE IF EXISTS sys_file_system_part;
CREATE TABLE sys_file_system_part (
    sys_file_system_part_id int NOT NULL AUTO_INCREMENT COMMENT '閸掑棛澧朓D - 娑撳鏁懛顏勵杻',
    sys_file_system_part_file_id int NOT NULL COMMENT '閺傚洣娆D - 閸忓疇浠坰ys_file_system鐞?,
    sys_file_system_part_seq int NOT NULL COMMENT '閸掑棛澧栨惔蹇撳娇 - 閸掑棛澧栭崷銊︽瀮娴犳湹鑵戦惃鍕€庢惔蹇撳娇閿涘奔绮?瀵偓婵?,
    sys_file_system_part_size bigint NULL DEFAULT NULL COMMENT '閸掑棛澧栨径褍鐨?- 鐠囥儱鍨庨悧鍥╂畱婢堆冪毈(鐎涙濡?',
    sys_file_system_part_md5 varchar(32)   NULL DEFAULT NULL COMMENT '閸掑棛澧朚D5 - 鐠囥儱鍨庨悧鍥╂畱MD5閸濆牆绗囬崐?,
    sys_file_system_part_local_path varchar(500)   NULL DEFAULT NULL COMMENT '閺堫剙婀寸€涙ê鍋嶇捄顖氱窞 - 閸掑棛澧栭崷銊︽拱閸︽壆娈戞稉瀛樻鐎涙ê鍋嶇捄顖氱窞',
    sys_file_system_part_version int NULL DEFAULT 0 COMMENT '閻楀牊婀伴崣?- 閻劋绨稊鎰潎闁?,
    sys_file_system_part_status int NULL DEFAULT 0 COMMENT '閸掑棛澧栭悩鑸碘偓?- 0:缁涘绶熸稉濠佺炊, 1:娑撳﹣绱舵稉? 2:娑撳﹣绱剁€瑰本鍨? -1:娑撳﹣绱舵径杈Е',
    sys_file_system_part_upload_time datetime NULL DEFAULT NULL COMMENT '娑撳﹣绱堕弮鍫曟？ - 閸掑棛澧栨稉濠佺炊鐎瑰本鍨氶惃鍕闂?,
    sys_file_system_part_process_time datetime NULL DEFAULT NULL COMMENT '婢跺嫮鎮婇弮鍫曟？ - 閸掑棛澧栫悮顐㈩槱閻?閸氬牆鑻?閻ㄥ嫭妞傞梻?,
    sys_file_system_part_error_message varchar(1000)   NULL DEFAULT NULL COMMENT '闁挎瑨顕ゆ穱鈩冧紖 - 娑撳﹣绱舵径杈Е閺冨墎娈戦柨娆掝嚖濞戝牊浼?,
    sys_file_system_part_retry_count int NULL DEFAULT 0 COMMENT '闁插秷鐦▎鈩冩殶 - 娑撳﹣绱舵径杈Е閸氬海娈戦柌宥堢槸濞嗏剝鏆?,
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_file_system_part_id) USING BTREE,
    UNIQUE INDEX uk_file_seq (sys_file_system_part_file_id, sys_file_system_part_seq) USING BTREE COMMENT '閺傚洣娆㈤崪灞界碍閸欑柉浠堥崥鍫濇暜娑撯偓缁便垹绱?,
    INDEX idx_file_id (sys_file_system_part_file_id) USING BTREE COMMENT '閺傚洣娆D缁便垹绱?,
    INDEX idx_status (sys_file_system_part_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci 
COMMENT = '閺傚洣娆㈢化鑽ょ埠閸掑棛澧栫悰?- 鐎涙ê鍋嶉崚鍡欏娑撳﹣绱堕惃鍕槨娑擃亜鍨庨悧鍥︿繆閹? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 19. 鐠囬攱鐪版潻鍊熼嚋閸樼喎顫愮拋鏉跨秿鐞?(sys_request_trace_raw)
-- 閻劑鈧? 閹镐椒绠欓崠鏍ㄦ暈鐟欙絽鎳℃稉顓犳畱鐠囬攱鐪伴崢鐔奉潗娴滃娆㈤敍宀€鏁ゆ禍搴ｅ劰閻?閸掑嘲鐫?閻栴剝娅曢崚鍡樼€?-- =====================================================
DROP TABLE IF EXISTS sys_request_trace_raw;
CREATE TABLE sys_request_trace_raw (
    sys_request_trace_raw_id bigint NOT NULL AUTO_INCREMENT COMMENT '閸樼喎顫愮拋鏉跨秿ID - 娑撳鏁懛顏勵杻',
    sys_request_trace_time datetime NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴弮鍫曟？',
    sys_request_trace_trace_id varchar(64)   NULL DEFAULT NULL COMMENT '鏉╁€熼嚋ID',
    sys_request_trace_strategy_code varchar(128)   NULL DEFAULT NULL COMMENT '缁涙牜鏆愮紓鏍垳',
    sys_request_trace_method varchar(16)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴弬瑙勭《',
    sys_request_trace_path varchar(500)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪扮捄顖氱窞',
    sys_request_trace_route varchar(500)   NULL DEFAULT NULL COMMENT '鐠侯垳鏁卞Ο鈩冩緲',
    sys_request_trace_query text   NULL COMMENT '閺屻儴顕楅崣鍌涙殶',
    sys_request_trace_url varchar(1000)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崷鏉挎絻',
    sys_request_trace_status int NULL DEFAULT NULL COMMENT '閸濆秴绨查悩鑸碘偓浣虹垳',
    sys_request_trace_duration_ms bigint NULL DEFAULT NULL COMMENT '閼版妞傚В顐ゎ潡',
    sys_request_trace_client_ip varchar(64)   NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩鐤楶',
    sys_request_trace_browser varchar(64)   NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳?,
    sys_request_trace_user_agent varchar(1024)   NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳銊︾垼鐠?,
    sys_request_trace_app_name varchar(255)   NULL DEFAULT NULL COMMENT '鎼存梻鏁ら崥宥囆?,
    sys_request_trace_user_id varchar(64)   NULL DEFAULT NULL COMMENT '閻劍鍩汭D',
    sys_request_trace_username varchar(255)   NULL DEFAULT NULL COMMENT '閻劍鍩涢崥?,
    sys_request_trace_fingerprint varchar(255)   NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩顖涘瘹缁?,
    sys_request_trace_token_hash varchar(255)   NULL DEFAULT NULL COMMENT '娴犮倗澧濋幗妯款洣',
    sys_request_trace_signals varchar(255)   NULL DEFAULT NULL COMMENT '閸涙垝鑵戞穱鈥冲娇',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_request_trace_raw_id) USING BTREE,
    INDEX idx_request_time (sys_request_trace_time) USING BTREE COMMENT '鐠囬攱鐪伴弮鍫曟？缁便垹绱?,
    INDEX idx_trace_id (sys_request_trace_trace_id) USING BTREE COMMENT '鏉╁€熼嚋ID缁便垹绱?,
    INDEX idx_strategy_code (sys_request_trace_strategy_code) USING BTREE COMMENT '缁涙牜鏆愮紓鏍垳缁便垹绱?,
    INDEX idx_client_ip (sys_request_trace_client_ip) USING BTREE COMMENT '鐎广垺鍩涚粩鐤楶缁便垹绱?,
    INDEX idx_user_id (sys_request_trace_user_id) USING BTREE COMMENT '閻劍鍩汭D缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '鐠囬攱鐪版潻鍊熼嚋閸樼喎顫愮拋鏉跨秿鐞?- 閹镐椒绠欓崠鏍攽娑撳搫鍨庨弸鎰斧婵绨ㄦ禒? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 20. 鐠囬攱鐪版潻鍊熼嚋鐞涘奔璐熼崚鍡樼€界紒鎾寸亯鐞?(sys_request_trace_behavior)
-- 閻劑鈧? 閹镐椒绠欓崠鏈匢/鐟欏嫬鍨崚鍡樼€介崥搴ｆ畱妞嬪酣娅撶紒鎾寸亯閿涘瞼鏁ゆ禍搴☆吀鐠佲€茬瑢婢跺嫮鐤?-- =====================================================
DROP TABLE IF EXISTS sys_request_trace_behavior;
CREATE TABLE sys_request_trace_behavior (
    sys_request_trace_behavior_id bigint NOT NULL AUTO_INCREMENT COMMENT '鐞涘奔璐熺紒鎾寸亯ID - 娑撳鏁懛顏勵杻',
    sys_request_trace_request_time datetime NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴弮鍫曟？',
    sys_request_trace_analyzed_at datetime NULL DEFAULT NULL COMMENT '閸掑棙鐎介弮鍫曟？',
    sys_request_trace_trace_id varchar(64)   NULL DEFAULT NULL COMMENT '鏉╁€熼嚋ID',
    sys_request_trace_strategy_code varchar(128)   NULL DEFAULT NULL COMMENT '缁涙牜鏆愮紓鏍垳',
    sys_request_trace_route varchar(500)   NULL DEFAULT NULL COMMENT '鐠侯垳鏁?,
    sys_request_trace_url varchar(1000)   NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崷鏉挎絻',
    sys_request_trace_status int NULL DEFAULT NULL COMMENT '閸濆秴绨查悩鑸碘偓浣虹垳',
    sys_request_trace_duration_ms bigint NULL DEFAULT NULL COMMENT '閼版妞傚В顐ゎ潡',
    sys_request_trace_client_ip varchar(64)   NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩鐤楶',
    sys_request_trace_browser varchar(64)   NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳?,
    sys_request_trace_user_id varchar(64)   NULL DEFAULT NULL COMMENT '閻劍鍩汭D',
    sys_request_trace_username varchar(255)   NULL DEFAULT NULL COMMENT '閻劍鍩涢崥?,
    sys_request_trace_fingerprint varchar(255)   NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩顖涘瘹缁?,
    sys_request_trace_user_agent varchar(1024)   NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳銊︾垼鐠?,
    sys_request_trace_signals varchar(255)   NULL DEFAULT NULL COMMENT '閸涙垝鑵戞穱鈥冲娇',
    sys_request_trace_related_request_count bigint NULL DEFAULT NULL COMMENT '閸忓疇浠堢拠閿嬬湴閺佷即鍣?,
    sys_request_trace_distinct_path_count bigint NULL DEFAULT NULL COMMENT '閹殿偅寮跨捄顖氱窞閺佷即鍣?,
    sys_request_trace_risk_level varchar(32)   NULL DEFAULT NULL COMMENT '妞嬪酣娅撶粵澶岄獓',
    sys_request_trace_summary text   NULL COMMENT '鐟欏嫬鍨幗妯款洣',
    sys_request_trace_ai_summary text   NULL COMMENT 'AI閹芥顩?,
    sys_request_trace_extra text   NULL COMMENT '閹碘晛鐫嶆穱鈩冧紖',
    create_name varchar(255)   NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)   NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_request_trace_behavior_id) USING BTREE,
    INDEX idx_behavior_request_time (sys_request_trace_request_time) USING BTREE COMMENT '鐠囬攱鐪伴弮鍫曟？缁便垹绱?,
    INDEX idx_behavior_analyzed_at (sys_request_trace_analyzed_at) USING BTREE COMMENT '閸掑棙鐎介弮鍫曟？缁便垹绱?,
    INDEX idx_behavior_trace_id (sys_request_trace_trace_id) USING BTREE COMMENT '鏉╁€熼嚋ID缁便垹绱?,
    INDEX idx_behavior_strategy (sys_request_trace_strategy_code) USING BTREE COMMENT '缁涙牜鏆愮紓鏍垳缁便垹绱?,
    INDEX idx_behavior_risk_level (sys_request_trace_risk_level) USING BTREE COMMENT '妞嬪酣娅撶粵澶岄獓缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci
COMMENT = '鐠囬攱鐪版潻鍊熼嚋鐞涘奔璐熼崚鍡樼€界紒鎾寸亯鐞?- 閹镐椒绠欓崠鏍棑闂勨晛鍨庨弸鎰波鐠? ROW_FORMAT = DYNAMIC;




/*
 * 缁崵绮虹粻锛勬倞濡€虫健 - 閺佺増宓佹惔鎾冲灥婵瀵查懘姘拱
 * 
 * 閸栧懎鎯堢悰?
 *   - sys_dept: 缂佸嫮绮愰張鐑樼€悰?
 *   - sys_dept_principal_rel: 闁劑妫拹鐔荤煑娴滃搫鍙х化鏄忋€?
 *   - sys_role: 鐟欐帟澹婄悰?
 *   - sys_role_menu: 鐟欐帟澹?閼挎粌宕熼崗宕囬兇鐞?
 *   - sys_menu: 閼挎粌宕熺悰?
 *   - sys_mini_menu: 鐏忓繒鈻兼惔蹇氬綅閸楁洖宕遍悧鍥€?
 *   - sys_feedback: 缁崵绮洪崣宥夘洯鐞?
 *   - sys_user: 閻劍鍩涚悰?
 *   - sys_user_role: 閻劍鍩?鐟欐帟澹婇崗宕囬兇鐞?
 *   - sys_user_config: 閻劍鍩涢柊宥囩枂鐞?缁楊兛绗侀弬鍦拨鐎?
 *   - sys_user_layout: 閻劍鍩涚敮鍐ㄧ湰闁板秶鐤嗙悰?
 *   - sys_user_log: 閻劍鍩涢惂璇茬秿閺冦儱绻旂悰?
 *   - sys_sms_log: 閻厺淇婇崣鎴︹偓浣规）韫囨銆?
 *   - sys_log: 缁崵绮洪幙宥勭稊閺冦儱绻旂悰?
 *   - sys_sfc: 缂佸嫪娆㈡惔鎾广€?
 *   - sys_sfc_install: 閻劍鍩涚€瑰顥婄紒鍕鐞?
 *   - sys_user_third_party: 閻劍鍩涚粭顑跨瑏閺傚湱绮︾€规俺銆?
 *   - sys_wechat_user: 瀵邦喕淇婇悽銊﹀煕鐞?
 *   - sys_announcement: 缁崵绮洪崗顒€鎲＄悰?
 *   - sys_avatar_frame: 婢舵潙鍎氬鍡楃暰娑斿銆?
 *   - sys_user_avatar_frame: 閻劍鍩涙径鏉戝剼濡楀棗鍙ч懕鏃囥€?
 *
 * 鐠囧瓨妲? 閺堫剝鍓奸張顒€鍑￠崥鍫濊嫙閸?V1.1(sys_mini_menu)/V1.2(sys_feedback)/V1.3(avatar)/V1.4(sys_menu 瀵拌姤鐖ｉ崚? 婢х偤鍣洪崘鍛啇閵?
 *
 * @author System
 * @since 2025-01-01
 * @version 1.0
 */




-- =====================================================
-- 1. 缂佸嫮绮愰張鐑樼€悰?(sys_dept)
-- 閻劑鈧? 鐎涙ê鍋嶆导浣风瑹/缁崵绮洪惃鍕矋缂佸洦鐏﹂弸鍕繆閹垽绱濋弨顖涘瘮閺嶆垵鑸扮紒鎾寸€?-- =====================================================
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    sys_dept_id int NOT NULL AUTO_INCREMENT COMMENT '閺堢儤鐎疘D - 娑撳鏁懛顏勵杻',
    sys_dept_name varchar(255)  NULL DEFAULT NULL COMMENT '閺堢儤鐎崥宥囆?- 缂佸嫮绮?闁劑妫惃鍕▔缁€鍝勬倳缁?,
    sys_dept_pid int NULL DEFAULT NULL COMMENT '閻栬埖婧€閺嬪嚘D - 娑撳﹦楠囩紒鍕矏閻ㄥ嚘D閿涘矂銆婄痪褌璐?',
    sys_dept_tree_id varchar(255)  NULL DEFAULT NULL COMMENT '閺堢儤鐎柧鎹愮熅 - 娴犲孩鐗撮懞鍌滃仯閸掓澘缍嬮崜宥堝Ν閻愬湱娈慖D鐠侯垰绶為敍灞筋洤"0,1,2"',
    sys_dept_code varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫮绮愮紓鏍垳 - 閺堢儤鐎惃鍕暜娑撯偓娑撴艾濮熺紓鏍垳',
    sys_dept_principal varchar(255)  NULL DEFAULT NULL COMMENT '闁劑妫拹鐔荤煑娴?- 鐠囥儵鍎撮梻銊ф畱鐠愮喕鐭楁禍鍝勵潣閸?,
    sys_dept_contact varchar(255)  NULL DEFAULT NULL COMMENT '鐠愮喕鐭楁禍楦夸粓缁粯鏌熷?- 鐠愮喕鐭楁禍铏规畱閻絻鐦介幋鏍у従娴犳牞浠堢化缁樻煙瀵?,
    sys_dept_sort int NULL DEFAULT NULL COMMENT '閹烘帒绨导妯哄帥缁?- 閺佹澘鈧壈绉虹亸蹇氱Ш闂堢姴澧?,
    sys_dept_status int NULL DEFAULT NULL COMMENT '閻樿埖鈧?- 0:閸氼垳鏁? 1:缁備胶鏁?,
    sys_dept_remark varchar(255)  NULL DEFAULT NULL COMMENT '婢跺洦鏁?- 闂勫嫬濮炵拠瀛樻娣団剝浼?,
    sys_dept_data_permission int NULL DEFAULT NULL COMMENT '閺佺増宓侀弶鍐缁鐎?- 1:閸忋劑鍎撮崣顖濐潌, 2:閺堫兛姹夐崣顖濐潌, 3:閹碘偓閸︺劑鍎撮梻銊ュ讲鐟? 4:閹碘偓閸︺劑鍎撮梻銊ュ挤鐎涙劗楠囬崣顖濐潌, 5:闁瀚ㄩ惃鍕劥闂傘劌褰茬憴?,
    sys_dept_data_permission_dept_id varchar(255)  NULL DEFAULT NULL COMMENT '閺佺増宓侀弶鍐閸欘垵顫嗛柈銊╂，ID閸掓銆?- 瑜版挻鏆熼幑顔芥綀闂勬劗琚崹瀣╄礋5閺冭绱濈€涙ê鍋嶉崣顖濐潌閻ㄥ嫰鍎撮梻鈫朌閿涘矂鈧褰块崚鍡涙',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_dept_id) USING BTREE,
    INDEX idx_dept_pid (sys_dept_pid) USING BTREE COMMENT '閻栬埖婧€閺嬪嫮鍌ㄥ?,
    INDEX idx_dept_code (sys_dept_code) USING BTREE COMMENT '閺堢儤鐎紓鏍垳缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '缂佸嫮绮愰張鐑樼€悰?- 鐎涙ê鍋嶇化鑽ょ埠閻ㄥ嫰鍎撮梻?缂佸嫮绮愰弸鑸电€穱鈩冧紖' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 2. 闁劑妫拹鐔荤煑娴滃搫鍙х化鏄忋€?(sys_dept_principal_rel)
-- 閻劑鈧? 鐎涙ê鍋嶉柈銊╂，娑撳氦绀嬬拹锝勬眽閻ㄥ嫬顦跨€电懓顦块崗宕囬兇
-- =====================================================
DROP TABLE IF EXISTS sys_dept_principal_rel;
CREATE TABLE sys_dept_principal_rel (
    sys_dept_principal_rel_id int NOT NULL AUTO_INCREMENT COMMENT '閸忓磭閮碔D - 娑撳鏁懛顏勵杻',
    sys_dept_id int NOT NULL COMMENT '闁劑妫琁D - 閸忓疇浠堢紒鍕矏閺堢儤鐎悰?,
    sys_user_id int NOT NULL COMMENT '鐠愮喕鐭楁禍铏规暏閹寸īD - 閸忓疇浠堥悽銊﹀煕鐞?,
    PRIMARY KEY (sys_dept_principal_rel_id) USING BTREE,
    UNIQUE INDEX uk_dept_user (sys_dept_id, sys_user_id) USING BTREE COMMENT '闁劑妫拹鐔荤煑娴滃搫鏁稉鈧槐銏犵穿',
    INDEX idx_principal_user_id (sys_user_id) USING BTREE COMMENT '鐠愮喕鐭楁禍铏规暏閹撮鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '闁劑妫拹鐔荤煑娴滃搫鍙х化鏄忋€?- 鐎涙ê鍋嶉柈銊╂，鐠愮喕鐭楁禍铏规畱婢舵艾顕径姘彠缁? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 3. 鐟欐帟澹婄悰?(sys_role)
-- 閻劑鈧? 鐎涙ê鍋嶇化鑽ょ埠鐟欐帟澹婃穱鈩冧紖閿涘瞼鏁ゆ禍搴㈡綀闂勬劖甯堕崚?-- =====================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    sys_role_id int NOT NULL AUTO_INCREMENT COMMENT '鐟欐帟澹奍D - 娑撳鏁懛顏勵杻',
    sys_role_name varchar(255)  NULL DEFAULT NULL COMMENT '鐟欐帟澹婇崥宥囆?- 鐟欐帟澹婇惃鍕▔缁€鍝勬倳缁夊府绱濇俊?缁狅紕鎮婇崨?',
    sys_role_code varchar(255)  NULL DEFAULT NULL COMMENT '鐟欐帟澹婄紓鏍垳 - 鐟欐帟澹婇惃鍕暜娑撯偓閺嶅洩鐦戦惍渚婄礉婵?ADMIN"',
    sys_role_in_system int NULL DEFAULT NULL COMMENT '閺勵垰鎯佺化鑽ょ埠鐟欐帟澹?- 0:闂堢偟閮寸紒鐔活潡閼? 1:缁崵绮洪崘鍛枂鐟欐帟澹?娑撳秴褰查崚鐘绘珟)',
    sys_role_status int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀崥顖滄暏 - 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_role_sort int NULL DEFAULT NULL COMMENT '閹烘帒绨导妯哄帥缁?- 閺佹澘鈧壈绉虹亸蹇氱Ш闂堢姴澧?,
    sys_role_remark varchar(255)  NULL DEFAULT NULL COMMENT '婢跺洦鏁?- 鐟欐帟澹婇惃鍕嚛閺勫孩寮挎潻?,
    sys_role_hidden int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀梾鎰 - 0:閺勫墽銇? 1:闂呮劘妫?閸撳秶顏稉宥呯潔缁€?',
    sys_role_data_scope int NULL DEFAULT 1 COMMENT '閺佺増宓侀弶鍐閼煎啫娲?- 1:閸忋劑鍎? 2:閺堫剟鍎撮梻? 3:閺堫剟鍎撮梻銊ュ挤娴犮儰绗? 4:閺堫兛姹?,
    sys_role_data_scope_dept_ids varchar(1000)  NULL DEFAULT NULL COMMENT '閺佺増宓侀弶鍐闁劑妫梿鍡楁値 - 闁褰块崚鍡涙閻ㄥ嫰鍎撮梻鈫朌',
    sys_role_readable int NULL DEFAULT NULL COMMENT '鐟欐帟澹婇崣顖濐嚢閺夊啴妾烘担?- 娴滃矁绻橀崚鏈电秴鐞涖劎銇氶惃鍕嚢閺夊啴妾?,
    sys_role_writeable int NULL DEFAULT NULL COMMENT '鐟欐帟澹婇崣顖氬晸閺夊啴妾烘担?- 娴滃矁绻橀崚鏈电秴鐞涖劎銇氶惃鍕晸閺夊啴妾?,
    sys_role_executable int NULL DEFAULT NULL COMMENT '鐟欐帟澹婇崣顖涘⒔鐞涘本娼堥梽鎰秴 - 娴滃矁绻橀崚鏈电秴鐞涖劎銇氶惃鍕⒔鐞涘本娼堥梽?,
    sys_role_board_card int NULL DEFAULT NULL COMMENT '鐟欐帟澹婇崣顖滄箙閺夎法琚崹?- 鐠囥儴顫楅懝鎻掑讲閺屻儳婀呴惃鍕箙閺夎法琚崹?,
    sys_role_readonly int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀崣顏囶嚢鐟欐帟澹?- 0:閸? 1:閺?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_role_id) USING BTREE,
    UNIQUE INDEX uk_role_code (sys_role_code) USING BTREE COMMENT '鐟欐帟澹婄紓鏍垳閸烆垯绔寸槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '鐟欐帟澹婄悰?- 鐎涙ê鍋嶇化鑽ょ埠鐟欐帟澹婃穱鈩冧紖閿涘瞼鏁ゆ禍宥矪AC閺夊啴妾洪幒褍鍩? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 4. 閼挎粌宕熺悰?(sys_menu)
-- 閻劑鈧? 鐎涙ê鍋嶇化鑽ょ埠閼挎粌宕?閺夊啴妾洪悙閫涗繆閹垽绱濋弨顖涘瘮閺嶆垵鑸扮紒鎾寸€?-- =====================================================
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    sys_menu_id int NOT NULL AUTO_INCREMENT COMMENT '閼挎粌宕烮D - 娑撳鏁懛顏勵杻',
    sys_menu_name varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熼崥宥囆?鐠侯垳鏁遍崥? - 閻劋绨捄顖滄暠閸栧綊鍘ら惃鍕暜娑撯偓閸氬秶袨',
    sys_menu_path varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熺捄顖氱窞 - 閸撳秶顏捄顖滄暠鐠侯垰绶為敍灞筋洤"/manage/user"',
    sys_menu_i18n varchar(255)  NULL DEFAULT NULL COMMENT '閸ヤ粙妾崠鏉抏y - 閻劋绨径姘愁嚔鐟封偓閺勫墽銇?,
    sys_menu_title varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熼弽鍥暯 - 閺勫墽銇氶崷銊ф櫕闂堫澀绗傞惃鍕倳缁?,
    sys_menu_perm varchar(255)  NULL DEFAULT NULL COMMENT '閺夊啴妾洪弽鍥槕 - 婵?sys:user:add"',
    sys_menu_pid int NULL DEFAULT NULL COMMENT '閻栨儼褰嶉崡鏃綝 - 娑撳﹦楠囬懣婊冨礋閻ㄥ嚘D閿涘矂銆婄痪褌璐?',
    sys_menu_tree_id varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熺捄顖氱窞闁?- 娴犲孩鐗撮懞鍌滃仯閸掓澘缍嬮崜宥堝Ν閻愬湱娈慖D鐠侯垰绶?,
    sys_menu_component varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熺紒鍕鐠侯垰绶?- Vue缂佸嫪娆㈤惃鍕熅瀵板嫸绱濇俊?/manage/user/index"',
    sys_menu_icon varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熼崶鐐垼 - 閸ョ偓鐖ｉ崥宥囆為敍灞筋洤"ep:user"',
    sys_menu_extra_icon varchar(255)  NULL DEFAULT NULL COMMENT '閸欏厖鏅舵０婵嗩樆閸ョ偓鐖?- 閼挎粌宕熼崣鍏呮櫠閺勫墽銇氶惃鍕禈閺?,
    sys_menu_type int NULL DEFAULT NULL COMMENT '閼挎粌宕熺猾璇茬€?- 0:閼挎粌宕? 1:iframe, 2:婢舵牠鎽? 3:閹稿鎸?,
    sys_menu_sort int NULL DEFAULT NULL COMMENT '閹烘帒绨导妯哄帥缁?- 閺佹澘鈧壈绉虹亸蹇氱Ш闂堢姴澧?,
    sys_menu_hidden int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀梾鎰 - 0:閺勫墽銇? 1:闂呮劘妫?,
    sys_menu_show_parent int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀弰鍓с仛閻栧墎楠囬懣婊冨礋 - 0:閺勫墽銇? 1:闂呮劘妫岄悥鍓侀獓',
    sys_menu_hidden_tag int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀崷銊︾垼缁涚偓鐖梾鎰 - 0:閺勫墽銇? 1:闂呮劘妫?,
sys_menu_fixed_tag int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀崶鍝勭暰閺嶅洨顒?- 0:娑撳秴娴愮€? 1:閸ュ搫鐣鹃崷銊︾垼缁涚偓鐖?,
    sys_menu_is_tag tinyint NULL DEFAULT 0 COMMENT '閺勵垰鎯佸鈧崥顖濆綅閸楁洖绐橀弽?- 0:閸?1:閺?,
    sys_menu_tag_type varchar(20)  NULL DEFAULT NULL COMMENT '瀵拌姤鐖ｇ猾璇茬€?- danger/success/info/warning/primary',
    sys_menu_tag_name varchar(50)  NULL DEFAULT NULL COMMENT '瀵拌姤鐖ｉ崥宥囆?- 閻ｆ瑧鈹栭崚娆愭▔缁€鍝勬妇閻愮懓绐橀弽?,
    sys_menu_redirect varchar(255)  NULL DEFAULT NULL COMMENT '鐠侯垳鏁遍柌宥呯暰閸氭垵婀撮崸鈧?- 鐠佸潡妫剁拠銉ㄥ綅閸楁洘妞傜捄瀹犳祮閻ㄥ嫬婀撮崸鈧?,
    sys_menu_enter_transition varchar(255)  NULL DEFAULT NULL COMMENT '鏉╂稑婧€閸斻劎鏁?- 妞ょ敻娼版潻娑樺弳閺冨墎娈戦崝銊ф暰閺佸牊鐏?,
    sys_menu_leave_transition varchar(255)  NULL DEFAULT NULL COMMENT '缁傝婧€閸斻劎鏁?- 妞ょ敻娼扮粋璇茬磻閺冨墎娈戦崝銊ф暰閺佸牊鐏?,
    sys_menu_active_path varchar(255)  NULL DEFAULT NULL COMMENT '閼挎粌宕熷┑鈧ú鏄忕熅瀵?- 瑜版挸灏柊宥堫嚉鐠侯垰绶為弮鑸电负濞茬粯顒濋懣婊冨礋',
    sys_menu_frame_src varchar(255)  NULL DEFAULT NULL COMMENT '閸愬懎绁甸柧鐐复閸︽澘娼?- iframe閹存牕顦婚柧鍓ф畱URL',
    sys_menu_keep_alive int NULL DEFAULT NULL COMMENT '閺勵垰鎯佺紓鎾崇摠妞ょ敻娼?- 0:娑撳秶绱︾€? 1:缂傛挸鐡?keep-alive)',
    sys_menu_role varchar(255)  NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偠顫楅懝鑼椽閻?- 閸欘垵顔栭梻顔款嚉閼挎粌宕熼惃鍕潡閼硅尙绱惍渚婄礉闁褰块崚鍡涙',
    sys_menu_home int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀崣顏呮▔缁€鍝勬躬娑撳銆?- 0:閸? 1:閺?,
    sys_menu_engine int NULL DEFAULT NULL COMMENT '閺勵垰鎯乪ngine鐢啫鐪稉濠氥€?- 0:閸? 1:閺?,
    sys_menu_remind int NULL DEFAULT 0 COMMENT 'detach from menu processing: 0=as menu, 1=route only',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_menu_id DESC) USING BTREE,
    INDEX idx_menu_pid (sys_menu_pid) USING BTREE COMMENT '閻栨儼褰嶉崡鏇犲偍瀵?,
    INDEX idx_menu_perm (sys_menu_perm) USING BTREE COMMENT '閺夊啴妾洪弽鍥槕缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閼挎粌宕熺悰?- 鐎涙ê鍋嶇化鑽ょ埠閼挎粌宕熼崪灞惧瘻闁筋喗娼堥梽鎰繆閹? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 5. 鐏忓繒鈻兼惔蹇氬綅閸楁洖宕遍悧鍥€?(sys_mini_menu)
-- 閻劑鈧? 鐎涙ê鍋嶇亸蹇曗柤鎼村繘顩绘い闈涘幢閻楀洭鍘ょ純顕嗙礉娑撳侗C閼挎粌宕熺€瑰苯鍙忛崚鍡欘瀲
-- =====================================================
DROP TABLE IF EXISTS sys_mini_menu;
CREATE TABLE sys_mini_menu (
    sys_mini_menu_id int NOT NULL AUTO_INCREMENT COMMENT '鐏忓繒鈻兼惔蹇氬綅閸楁椌D - 娑撳鏁懛顏勵杻',
    sys_mini_menu_title varchar(255)  NULL DEFAULT NULL COMMENT '閸楋紕澧栭弽鍥暯',
    sys_mini_menu_subtitle varchar(255)  NULL DEFAULT NULL COMMENT '閸楋紕澧栭崜顖涚垼妫?,
    sys_mini_menu_category varchar(128)  NULL DEFAULT NULL COMMENT '閸楋紕澧栭崚鍡欑矋',
    sys_mini_menu_path varchar(255)  NULL DEFAULT NULL COMMENT '鐏忓繒鈻兼惔蹇涖€夐棃銏ｇ熅瀵?,
    sys_mini_menu_jump_mode int NULL DEFAULT 0 COMMENT '鐠哄疇娴嗗Ο鈥崇础 - 0:閺咁噣鈧岸銆夐棃? 1:tabBar妞ょ敻娼? 2:webview',
    sys_mini_menu_icon varchar(255)  NULL DEFAULT NULL COMMENT '閸楋紕澧栭崶鐐垼',
    sys_mini_menu_cover varchar(255)  NULL DEFAULT NULL COMMENT '閸楋紕澧栫亸渚€娼?閼冲本娅?,
    sys_mini_menu_badge varchar(64)  NULL DEFAULT NULL COMMENT '鐟欐帗鐖ｉ弬鍥攳',
    sys_mini_menu_badge_type varchar(64)  NULL DEFAULT NULL COMMENT '鐟欐帗鐖ｉ弽宄扮础',
    sys_mini_menu_perm varchar(255)  NULL DEFAULT NULL COMMENT '閺夊啴妾洪弽鍥槕',
    sys_mini_menu_role varchar(255)  NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偠顫楅懝鑼椽閻?,
    sys_mini_menu_sort int NULL DEFAULT 1 COMMENT '閹烘帒绨导妯哄帥缁?,
    sys_mini_menu_keep_alive int NULL DEFAULT 0 COMMENT '閺勵垰鎯佺紓鎾崇摠 - 0:閸? 1:閺?,
    sys_mini_menu_hidden int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀梾鎰 - 0:閺勫墽銇? 1:闂呮劘妫?,
    sys_mini_menu_description varchar(255)  NULL DEFAULT NULL COMMENT '閸楋紕澧栫拠瀛樻',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_mini_menu_id) USING BTREE,
    INDEX idx_mini_menu_sort (sys_mini_menu_sort) USING BTREE COMMENT '閹烘帒绨槐銏犵穿',
    INDEX idx_mini_menu_perm (sys_mini_menu_perm) USING BTREE COMMENT '閺夊啴妾洪弽鍥槕缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '鐏忓繒鈻兼惔蹇氬綅閸楁洖宕遍悧鍥€?- 鐎涙ê鍋嶇亸蹇曗柤鎼村繘顩绘い闈涘幢閻楀洣绗岄弶鍐闁板秶鐤? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 6. 鐟欐帟澹?閼挎粌宕熼崗宕囬兇鐞?(sys_role_menu)
-- 閻劑鈧? 鐎涙ê鍋嶇憴鎺曞娑撳氦褰嶉崡鏇犳畱婢舵艾顕径姘彠缁?-- =====================================================
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    sys_role_menu_id int NOT NULL AUTO_INCREMENT COMMENT '閸忓磭閮碔D - 娑撳鏁懛顏勵杻',
    sys_role_id int NULL DEFAULT NULL COMMENT '鐟欐帟澹奍D - 閸忓疇浠堢憴鎺曞鐞?,
    sys_menu_id int NULL DEFAULT NULL COMMENT '閼挎粌宕烮D - 閸忓疇浠堥懣婊冨礋鐞?,
    PRIMARY KEY (sys_role_menu_id) USING BTREE,
    INDEX idx_role_id (sys_role_id) USING BTREE COMMENT '鐟欐帟澹奍D缁便垹绱?,
    INDEX idx_menu_id (sys_menu_id) USING BTREE COMMENT '閼挎粌宕烮D缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '鐟欐帟澹?閼挎粌宕熼崗宕囬兇鐞?- 鐎涙ê鍋嶇憴鎺曞娑撳氦褰嶉崡鏇犳畱婢舵艾顕径姘Ё鐏忓嫬鍙х化? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 7. 缁崵绮洪崣宥夘洯鐞?(sys_feedback)
-- 閻劑鈧? 鐎涙ê鍋嶇敮顔煎И娑撳骸寮芥＃鍫涒偓浣哥磽鐢晲绗傞幎銉х搼閻劍鍩涢崣宥夘洯閸愬懎顔?-- =====================================================
DROP TABLE IF EXISTS sys_feedback;
CREATE TABLE sys_feedback (
    sys_feedback_id int NOT NULL AUTO_INCREMENT COMMENT '閸欏秹顩璉D - 娑撳鏁懛顏勵杻',
    sys_feedback_type varchar(255)  NULL DEFAULT NULL COMMENT '閸欏秹顩猾璇茬€?- 婵′经UG閵嗕讣UGGESTION閵嗕副THER',
    sys_feedback_images text  NULL COMMENT '閸欏秹顩崶鍓у - 婢舵矮閲滈崶鍓у閸︽澘娼冮柅妤€褰块崚鍡涙',
    sys_feedback_content text  NULL COMMENT '閸欏秹顩崘鍛啇 - 閻劍鍩涢幓鎰唉閻ㄥ嫰妫舵０妯诲伎鏉╃増鍨ㄥ楦款唴',
    sys_feedback_status int NULL DEFAULT 0 COMMENT '閸欏秹顩悩鑸碘偓?- 0:閺堫亜顦╅悶? 1:瀹告彃顦╅悶?,
    sys_feedback_deal_by int NULL DEFAULT NULL COMMENT '婢跺嫮鎮婃禍绡扗 - 閸忓疇浠堢化鑽ょ埠閻劍鍩?,
    sys_feedback_deal_name varchar(255)  NULL DEFAULT NULL COMMENT '婢跺嫮鎮婃禍鍝勬倳缁?,
    sys_feedback_recover_content text  NULL COMMENT '閸ョ偛顦查崘鍛啇 - 缁狅紕鎮婇崨妯侯槱閻炲棗娲栨径?,
    sys_feedback_recover_time datetime NULL DEFAULT NULL COMMENT '閸ョ偛顦查弮鍫曟？ - 婢跺嫮鎮婇崣宥夘洯閻ㄥ嫭妞傞梻?,
sys_feedback_recover_images text  NULL COMMENT '閸ョ偛顦查崶鍓у - 缁狅紕鎮婇崨妯烘礀婢跺秹妾崶?,
    sys_feedback_user_agent varchar(500)  NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩顖滆閸?UserAgent)',
    sys_feedback_platform varchar(50)  NULL DEFAULT NULL COMMENT '楠炲啿褰寸猾璇茬€?妫板嫮鏆€)',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_feedback_id) USING BTREE,
    INDEX idx_feedback_type (sys_feedback_type) USING BTREE COMMENT '閸欏秹顩猾璇茬€风槐銏犵穿',
    INDEX idx_feedback_status (sys_feedback_status) USING BTREE COMMENT '閸欏秹顩悩鑸碘偓浣哄偍瀵?,
    INDEX idx_feedback_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '缁崵绮洪崣宥夘洯鐞?- 鐎涙ê鍋嶇敮顔煎И娑撳骸寮芥＃鍫濇嫲瀵倸鐖舵稉濠冨Г娣団剝浼? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 閻劍鍩涚悰?(sys_user)
-- 閻劑鈧? 鐎涙ê鍋嶇化鑽ょ埠閻劍鍩涚拹锕€褰挎穱鈩冧紖
-- =====================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    sys_user_id int NOT NULL AUTO_INCREMENT COMMENT '閻劍鍩汭D - 娑撳鏁懛顏勵杻',
    sys_user_username varchar(255)  NULL DEFAULT NULL COMMENT '閻劍鍩涚拹锕€褰?- 閻ц缍嶆担璺ㄦ暏閻ㄥ嫬鏁稉鈧拹锕€褰块崥?,
    sys_user_nickname varchar(255)  NULL DEFAULT NULL COMMENT '閻劍鍩涢弰鐢敌?- 閺勫墽銇氶崥宥囆?,
    sys_user_password varchar(255)  NULL DEFAULT NULL COMMENT '閻劍鍩涚€靛棛鐖?- 閸旂姴鐦戦崥搴ｆ畱鐎靛棛鐖?,
    sys_user_salt varchar(255)  NULL DEFAULT NULL COMMENT '鐎靛棛鐖滈惄鎰偓?- 閻劋绨€靛棛鐖滈崝鐘茬槕閻ㄥ嫰娈㈤張鍝勭摟缁楋缚瑕?,
    sys_user_delete int NULL DEFAULT NULL COMMENT '閺勵垰鎯侀崚鐘绘珟 - 0:閺堫亜鍨归梽? 1:瀹告彃鍨归梽?闁槒绶崚鐘绘珟)',
    sys_user_status int NULL DEFAULT NULL COMMENT '閻劍鍩涢悩鑸碘偓?- 0:缁備胶鏁? 1:閸氼垳鏁?,
    sys_user_phone varchar(255)  NULL DEFAULT NULL COMMENT '閹靛婧€閸欓鐖?- 閻劍鍩涢惃鍕閺堝搫褰?,
    sys_user_email varchar(255)  NULL DEFAULT NULL COMMENT '閻㈤潧鐡欓柇顔绢唸 - 閻劍鍩涢惃鍕仏缁犲崬婀撮崸鈧?,
    sys_user_sex int NULL DEFAULT NULL COMMENT '閻劍鍩涢幀褍鍩?- 0:婵? 1:閻? 2:閸忔湹绮?,
    sys_user_full_address varchar(400)  NULL DEFAULT NULL COMMENT '鐏炲懍缍囬崷鏉挎絻 - 閻劍鍩涢惃鍕嚊缂佸棗婀撮崸鈧?,
    sys_user_in_system int NULL DEFAULT NULL COMMENT '閺勵垰鎯佺化鑽ょ埠鐠愶箑褰?- 0:閺咁噣鈧俺澶勯崣? 1:缁崵绮洪崘鍛枂鐠愶箑褰?娑撳秴褰查崚鐘绘珟)',
    sys_user_avatar text  NULL COMMENT '閻劍鍩涙径鏉戝剼 - 婢舵潙鍎氶崶鍓у閻ㄥ垊RL閹存渾ase64',
    sys_user_card varchar(255)  NULL DEFAULT NULL COMMENT '闊偂鍞ょ拠浣稿娇 - 閻劍鍩涢惃鍕煩娴犲€熺槈閸欓鐖?,
    sys_dept_id int NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偤鍎撮梻鈫朌 - 閸忓疇浠堢紒鍕矏閺堢儤鐎悰?,
    sys_user_remark varchar(255)  NULL DEFAULT NULL COMMENT '閻劍鍩涙径鍥ㄦ暈 - 闂勫嫬濮炵拠瀛樻娣団剝浼?,
    sys_user_last_address varchar(255)  NULL DEFAULT NULL COMMENT '閺堚偓閸氬海娅ヨぐ鏇炴勾閻?- 閺堚偓鏉╂垳绔村▎锛勬瑜版洜娈戦崷鎵倞娴ｅ秶鐤?,
    sys_user_register_address varchar(255)  NULL DEFAULT NULL COMMENT '濞夈劌鍞介崷鎵仯 - 閻劍鍩涘▔銊ュ斀閺冨墎娈戦崷鎵倞娴ｅ秶鐤?,
    sys_user_last_ip varchar(255)  NULL DEFAULT NULL COMMENT '閺堚偓閸氬海娅ヨぐ鏃綪 - 閺堚偓鏉╂垳绔村▎锛勬瑜版洜娈慖P閸︽澘娼?,
    sys_user_register_ip varchar(255)  NULL DEFAULT NULL COMMENT '濞夈劌鍞絀P - 閻劍鍩涘▔銊ュ斀閺冨墎娈慖P閸︽澘娼?,
    sys_user_login_count int NULL DEFAULT NULL COMMENT '閻ц缍嶅▎鈩冩殶 - 缁鳖垵顓搁惂璇茬秿濞嗏剝鏆?,
    sys_client_id varchar(255)  NULL DEFAULT NULL COMMENT '鐎广垺鍩涚粩鐤楧 - 缂佹垵鐣鹃惃鍕吂閹撮顏弽鍥槕',
    sys_user_totp_code varchar(600)  NULL DEFAULT NULL COMMENT 'TOTP鐎靛棝鎸?- 娑撱倖顒炴宀冪槈閻ㄥ嫬濮炵€靛棗鐦戦柦?Base32缂傛牜鐖?',
    sys_user_birthday varchar(20)  NULL DEFAULT NULL COMMENT '閻㈢喐妫?- 閻劍鍩涢崙铏规晸閺冦儲婀￠敍宀€鏁ゆ禍搴ｆ晸閺冦儱銇旈崓蹇旑攱缁涘閲滈幀褍瀵查崝鐔诲厴',
    sys_user_avatar_frame int NULL DEFAULT NULL COMMENT '婢舵潙鍎氬鍜篋 - 閻劍鍩涢柅澶嬪閻ㄥ嫬銇旈崓蹇旑攱閺嶅嘲绱D閿?娑撴椽绮拋?,
    sys_user_avatar_frame_show int NULL DEFAULT 0 COMMENT '閺勵垰鎯侀弰鍓с仛婢舵潙鍎氬?- 0:娑撳秵妯夌粈?1:閺勫墽銇?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_user_id) USING BTREE,
    UNIQUE INDEX uk_username (sys_user_username) USING BTREE COMMENT '閻劍鍩涢崥宥呮暜娑撯偓缁便垹绱?,
    INDEX idx_dept_id (sys_dept_id) USING BTREE COMMENT '闁劑妫琁D缁便垹绱?,
    INDEX idx_phone (sys_user_phone) USING BTREE COMMENT '閹靛婧€閸欓鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涚悰?- 鐎涙ê鍋嶇化鑽ょ埠閻劍鍩涚拹锕€褰块崺鐑樻拱娣団剝浼? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 9. 閻劍鍩?鐟欐帟澹婇崗宕囬兇鐞?(sys_user_role)
-- 閻劑鈧? 鐎涙ê鍋嶉悽銊﹀煕娑撳氦顫楅懝鑼畱婢舵艾顕径姘彠缁?-- =====================================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    sys_user_role_id int NOT NULL AUTO_INCREMENT COMMENT '閸忓磭閮碔D - 娑撳鏁懛顏勵杻',
    sys_user_id int NULL DEFAULT NULL COMMENT '閻劍鍩汭D - 閸忓疇浠堥悽銊﹀煕鐞?,
    sys_role_id int NULL DEFAULT NULL COMMENT '鐟欐帟澹奍D - 閸忓疇浠堢憴鎺曞鐞?,
    PRIMARY KEY (sys_user_role_id) USING BTREE,
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '閻劍鍩汭D缁便垹绱?,
    INDEX idx_role_id (sys_role_id) USING BTREE COMMENT '鐟欐帟澹奍D缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩?鐟欐帟澹婇崗宕囬兇鐞?- 鐎涙ê鍋嶉悽銊﹀煕娑撳氦顫楅懝鑼畱婢舵艾顕径姘Ё鐏忓嫬鍙х化? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 閻劍鍩涢柊宥囩枂鐞?(sys_user_config)
-- 閻劑鈧? 鐎涙ê鍋嶉悽銊﹀煕閻ㄥ嫮顑囨稉澶嬫煙鐠愶箑褰跨紒鎴濈暰娣団剝浼?-- =====================================================
DROP TABLE IF EXISTS sys_user_config;
CREATE TABLE sys_user_config (
    sys_user_config_id int NOT NULL COMMENT '閻劍鍩涢柊宥囩枂ID - 娑撳海鏁ら幋绋〥娑撯偓閼?娑撯偓鐎甸€涚)',
    sys_user_config_wx_openid varchar(255)  NULL DEFAULT NULL COMMENT '瀵邦喕淇奜penID - 瀵邦喕淇婇惂璇茬秿閻ㄥ嚠penID',
    sys_user_config_wx_unionid varchar(255)  NULL DEFAULT NULL COMMENT '瀵邦喕淇奤nionID - 瀵邦喕淇婂鈧弨鎯ч挬閸欐壆娈慤nionID',
    sys_user_config_gitee_id varchar(255)  NULL DEFAULT NULL COMMENT 'Gitee閻劍鍩汭D - Gitee閻ц缍嶇紒鎴濈暰閻ㄥ嫮鏁ら幋绋〥',
    sys_user_config_github_id varchar(255)  NULL DEFAULT NULL COMMENT 'GitHub閻劍鍩汭D - GitHub閻ц缍嶇紒鎴濈暰閻ㄥ嫮鏁ら幋绋〥',
    sys_user_config_microsoft_id varchar(255)  NULL DEFAULT NULL COMMENT '瀵邦喛钂嬬拹锕€褰縄D - Microsoft閻ц缍嶇紒鎴濈暰閻ㄥ嫮鏁ら幋绋〥',
    sys_user_config_google_id varchar(255)  NULL DEFAULT NULL COMMENT 'Google鐠愶箑褰縄D - Google閻ц缍嶇紒鎴濈暰閻ㄥ嫮鏁ら幋绋〥',
    sys_user_config_qq_openid varchar(255)  NULL DEFAULT NULL COMMENT 'QQ OpenID - QQ閻ц缍嶉惃鍑畃enID',
    sys_user_config_dingtalk_id varchar(255)  NULL DEFAULT NULL COMMENT '闁藉鎷ら悽銊﹀煕ID - 闁藉鎷ら惂璇茬秿缂佹垵鐣鹃惃鍕暏閹寸īD',
    sys_user_config_feishu_id varchar(255)  NULL DEFAULT NULL COMMENT '妞嬬偘鍔熼悽銊﹀煕ID - 妞嬬偘鍔熼惂璇茬秿缂佹垵鐣鹃惃鍕暏閹寸īD',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_user_config_id) USING BTREE,
    INDEX idx_wx_openid (sys_user_config_wx_openid) USING BTREE COMMENT '瀵邦喕淇奜penID缁便垹绱?,
    INDEX idx_gitee_id (sys_user_config_gitee_id) USING BTREE COMMENT 'Gitee ID缁便垹绱?,
    INDEX idx_microsoft_id (sys_user_config_microsoft_id) USING BTREE COMMENT '瀵邦喛钂嬬拹锕€褰跨槐銏犵穿'
) ENGINE = InnoDB CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涢柊宥囩枂鐞?- 鐎涙ê鍋嶉悽銊﹀煕缁楊兛绗侀弬纭呭閸欓绮︾€规矮淇婇幁? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 8. 閻劍鍩涚敮鍐ㄧ湰闁板秶鐤嗙悰?(sys_user_layout)
-- 閻劑鈧? 鐎涙ê鍋嶉悽銊﹀煕娑擃亝鈧冨閻ㄥ嫮鏅棃銏犵鐏炩偓闁板秶鐤?-- =====================================================
DROP TABLE IF EXISTS sys_user_layout;
CREATE TABLE sys_user_layout (
    sys_layout_id int NOT NULL AUTO_INCREMENT COMMENT '鐢啫鐪琁D - 娑撳鏁懛顏勵杻',
    sys_layout_layout varchar(255)  NULL DEFAULT NULL COMMENT '鐢啫鐪柊宥囩枂 - JSON閺嶇厧绱￠惃鍕秹閺嶇厧绔风仦鈧穱鈩冧紖',
    sys_layout_grid varchar(255)  NULL DEFAULT NULL COMMENT '缂冩垶鐗搁柊宥囩枂 - JSON閺嶇厧绱￠惃鍕秹閺嶇厧寮弫?,
    sys_layout_component varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤柊宥囩枂 - JSON閺嶇厧绱￠惃鍕矋娴犺泛鍨悰?,
    sys_layout_user_id int NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偟鏁ら幋绋〥 - 閸忓疇浠堥悽銊﹀煕鐞?,
    sys_layout_preference text  NULL COMMENT '閻劍鍩涢崑蹇撱偨闁板秶鐤?- lay-setting 闁板秶鐤嗛敍鍦漇ON閺嶇厧绱￠敍?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_layout_id) USING BTREE,
    UNIQUE INDEX uk_user_id (sys_layout_user_id) USING BTREE COMMENT '閻劍鍩汭D閸烆垯绔寸槐銏犵穿'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涚敮鍐ㄧ湰闁板秶鐤嗙悰?- 鐎涙ê鍋嶉悽銊﹀煕娑擃亝鈧冨閻ㄥ嫮鏅棃銏犵鐏炩偓' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 9. 閻劍鍩涢惂璇茬秿閺冦儱绻旂悰?(sys_user_log)
-- 閻劑鈧? 鐠佹澘缍嶉悽銊﹀煕閻ц缍嶉惂璇插毉閹垮秳缍旈弮銉ョ箶
-- =====================================================
DROP TABLE IF EXISTS sys_user_log;
CREATE TABLE sys_user_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '閺冦儱绻擨D - 娑撳鏁懛顏勵杻',
    sys_log_username varchar(255)  NULL DEFAULT NULL COMMENT '閻ц缍嶇拹锕€褰?- 閻劍鍩涢惂璇茬秿娴ｈ法鏁ら惃鍕閸?,
    sys_log_address text  NULL COMMENT '閻ц缍嶉崷鎵仯 - 閺嶈宓両P鐟欙絾鐎介惃鍕勾閻炲棔缍呯純?,
    sys_log_ip varchar(255)  NULL DEFAULT NULL COMMENT '閻ц缍岻P - 閻劍鍩涢惂璇茬秿閺冨墎娈慖P閸︽澘娼?,
    sys_log_isp varchar(255)  NULL DEFAULT NULL COMMENT '鏉╂劘鎯€閸?- 缂冩垹绮舵潻鎰儉閸熷棔淇婇幁?,
    sys_log_system varchar(255)  NULL DEFAULT NULL COMMENT '閹垮秳缍旂化鑽ょ埠 - 閻劍鍩涙担璺ㄦ暏閻ㄥ嫭鎼锋担婊呴兇缂?,
    sys_log_ua text  NULL COMMENT 'User-Agent - 濞村繗顫嶉崳銊ョ暚閺佸娈慤A鐎涙顑佹稉?,
    sys_log_browser varchar(255)  NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳?- 濞村繗顫嶉崳銊ц閸ㄥ鎷伴悧鍫熸拱',
    sys_log_url text  NULL COMMENT '鐠囬攱鐪伴崷鏉挎絻 - 閻ц缍嶉惃鍑橮I閸︽澘娼?,
    sys_log_app_name varchar(255)  NULL DEFAULT NULL COMMENT '鎼存梻鏁ら崥宥囆?- 鐠囬攱鐪伴弶銉︾爱鎼存梻鏁ら弽鍥槕',
    sys_log_param text  NULL COMMENT '鐠囬攱鐪伴崣鍌涙殶 - 閻ц缍嶇拠閿嬬湴閻ㄥ嫬寮弫?閼磋鲸鏅?',
    sys_log_cost int NULL DEFAULT NULL COMMENT '閼版妞?濮ｎ偆顫? - 閻ц缍嶉幙宥勭稊閼版妞?,
    sys_log_from varchar(255)  NULL DEFAULT NULL COMMENT '閹垮秳缍旂猾璇茬€?- LOGIN:閻ц缍? LOGOUT:閻ц鍤?,
    sys_log_status int NULL DEFAULT NULL COMMENT '閹垮秳缍旈悩鑸碘偓?- 0:婢惰精瑙? 1:閹存劕濮?,
    sys_log_message text  NULL COMMENT '閸濆秴绨插☉鍫熶紖 - 閻ц缍?閻ц鍤紒鎾寸亯閹诲繗鍫?,
    sys_log_fingerprint varchar(255)  NULL DEFAULT NULL COMMENT '濞村繗顫嶉崳銊﹀瘹缁?- 閻劋绨拋鎯ь槵鐠囧棗鍩嗛惃鍕瘹缁?,
    sys_log_login_type varchar(255)  NULL DEFAULT NULL COMMENT '閻ц缍嶉弬鐟扮础 - WEB:缂冩垿銆? APP:鎼存梻鏁? GITEE:Gitee缁?,
    sys_dept_id int NULL DEFAULT NULL COMMENT '閹碘偓鐏炵偞婧€閺嬪嚘D - 閻劍鍩涢幍鈧仦鐐垫畱闁劑妫琁D',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗(閻劍鍩汭D)',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '閻劍鍩涢崥宥囧偍瀵?,
    INDEX idx_from (sys_log_from) USING BTREE COMMENT '閹垮秳缍旂猾璇茬€风槐銏犵穿',
    INDEX idx_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涢惂璇茬秿閺冦儱绻旂悰?- 鐠佹澘缍嶉悽銊﹀煕閻ц缍嶉惂璇插毉閹垮秳缍? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 10. 閻厺淇婇弮銉ョ箶鐞?(sys_sms_log)
-- 閻劑鈧? 鐠佹澘缍嶉惌顓濅繆閸欐垿鈧礁宸婚崣?-- =====================================================
DROP TABLE IF EXISTS sys_sms_log;
CREATE TABLE sys_sms_log (
    sys_sms_log_id int NOT NULL AUTO_INCREMENT COMMENT '閺冦儱绻擨D - 娑撳鏁懛顏勵杻',
    sys_sms_log_target varchar(255)  NULL DEFAULT NULL COMMENT '閹恒儲鏁规禍?- 閹靛婧€閸欓鐖?,
    sys_sms_log_template_id varchar(255)  NULL DEFAULT NULL COMMENT '濡剝婢業D - 娴ｈ法鏁ら惃鍕侀弶璺ㄧ椽閻?,
    sys_sms_log_param varchar(255)  NULL DEFAULT NULL COMMENT '濡剝婢橀崣鍌涙殶 - JSON閺嶇厧绱￠惃鍕侀弶鍨綁闁?,
    sys_sms_log_content longtext  NULL COMMENT '閸欐垿鈧礁鍞寸€?- 閺堚偓缂佸牆褰傞柅浣烘畱閻厺淇婇崘鍛啇',
    sys_sms_cost decimal(10, 2) NULL DEFAULT NULL COMMENT '閼版妞?- 閸欐垿鈧浇鈧妞?濮ｎ偆顫?',
    sys_sms_code varchar(255)  NULL DEFAULT NULL COMMENT '鏉╂柨娲栭惍?- 閻厺淇婇獮鍐插酱鏉╂柨娲栭惃鍕Ц閹胶鐖?,
    sys_sms_message text  NULL COMMENT '鏉╂柨娲栧☉鍫熶紖 - 閻厺淇婇獮鍐插酱鏉╂柨娲栭惃鍕Х閹?,
    sys_sms_app_type varchar(255)  NULL DEFAULT NULL COMMENT '閻厺淇婇崢鍌氼啀 - 娴ｈ法鏁ら惃鍕叚娣団剝婀囬崝鈥虫櫌',
    sys_sms_app_id varchar(255)  NULL DEFAULT NULL COMMENT '閻厺淇夾ppID',
    sys_sms_app_sign varchar(255)  NULL DEFAULT NULL COMMENT '閻厺淇婄粵鎯ф倳',
    sys_project_id int NULL DEFAULT NULL COMMENT '妞ゅ湱娲癐D - 閸忓疇浠坰ys_project鐞?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_sms_log_id) USING BTREE,
    INDEX idx_target (sys_sms_log_target) USING BTREE COMMENT '閹恒儲鏁规禍铏瑰偍瀵?,
    INDEX idx_project_id (sys_project_id) USING BTREE COMMENT '妞ゅ湱娲癐D缁便垹绱?,
    INDEX idx_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻厺淇婇弮銉ョ箶鐞?- 鐠佹澘缍嶉惌顓濅繆閸欐垿鈧礁宸婚崣? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 11. 缁崵绮洪幙宥勭稊閺冦儱绻旂悰?(sys_log)
-- 閻劑鈧? 鐠佹澘缍嶇化鑽ょ埠闁氨鏁ら幙宥勭稊閺冦儱绻?-- =====================================================
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    sys_log_id int NOT NULL AUTO_INCREMENT COMMENT '閺冦儱绻擨D - 娑撳鏁懛顏勵杻',
    sys_log_username varchar(255)  NULL DEFAULT NULL COMMENT '閹垮秳缍旂拹锕€褰?- 閹笛嗩攽閹垮秳缍旈惃鍕暏閹寸柉澶勯崣?,
    sys_log_address varchar(255)  NULL DEFAULT NULL COMMENT '鐠囬攱鐪癐P - 閸欐垼鎹ｇ拠閿嬬湴閻ㄥ嚘P閸︽澘娼?,
    sys_log_url varchar(255)  NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崷鏉挎絻 - API閹恒儱褰涚捄顖氱窞',
    sys_log_param varchar(255)  NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴崣鍌涙殶 - 閹恒儱褰涚拠閿嬬湴閸欏倹鏆?,
    sys_log_from varchar(255)  NULL DEFAULT NULL COMMENT '鐠囬攱鐪板Ο鈥虫健 - 閸旂喕鍏樺Ο鈥虫健閸氬秶袨',
    sys_log_status int NULL DEFAULT NULL COMMENT '鐠囬攱鐪伴悩鑸碘偓?- 0:婢惰精瑙? 1:閹存劕濮?,
    sys_log_cost int NULL DEFAULT NULL COMMENT '閼版妞?濮ｎ偆顫? - 閹恒儱褰涢崫宥呯安閺冨爼妫?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_log_id) USING BTREE,
    INDEX idx_username (sys_log_username) USING BTREE COMMENT '閻劍鍩涢崥宥囧偍瀵?,
    INDEX idx_create_time (create_time) USING BTREE COMMENT '閸掓稑缂撻弮鍫曟？缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '缁崵绮洪幙宥勭稊閺冦儱绻旂悰?- 鐠佹澘缍嶇化鑽ょ埠闁氨鏁ら幙宥勭稊閺冦儱绻? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 缂佸嫪娆㈡惔鎾广€?(sys_sfc)
-- 閻劑鈧? 鐎涙ê鍋嶉崣顖炲帳缂冾喚娈戦崜宥囶伂缂佸嫪娆㈡穱鈩冧紖
-- =====================================================
DROP TABLE IF EXISTS sys_sfc;
CREATE TABLE sys_sfc (
    sys_sfc_id int NOT NULL AUTO_INCREMENT COMMENT '缂佸嫪娆D - 娑撳鏁懛顏勵杻',
    sys_sfc_name varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤崥宥囆?閼昏鲸鏋? - 缂佸嫪娆㈤惃鍕暜娑撯偓閺嶅洩鐦戦崥?,
    sys_sfc_chinese_name varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤崥宥囆?娑擃厽鏋? - 缂佸嫪娆㈤惃鍕▔缁€鍝勬倳缁?,
    sys_sfc_function varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤崝鐔诲厴 - 婢舵矮閲滈崝鐔诲厴閻劑鈧褰块崚鍡涙',
    sys_sfc_icon varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤崶鐐垼 - 閸ョ偓鐖ｉ崥宥囆?,
    sys_sfc_category varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤崚鍡欒 - 婵′径OME鐞涖劎銇氭＃鏍€夌紒鍕',
    sys_sfc_path varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈢捄顖氱窞 - 缂佸嫪娆㈤弬鍥︽鐎涙ɑ鏂佺捄顖氱窞閹存湧RL',
    sys_sfc_status int NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤悩鑸碘偓?- 0:閺堫亜鎯庨悽? 1:瀹告彃鎯庨悽?,
    sys_sfc_desc varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤幓蹇氬牚 - 缂佸嫪娆㈤崝鐔诲厴鐠囧瓨妲?,
    sys_sfc_proxy varchar(255)  NULL DEFAULT NULL COMMENT '娴狅絿鎮婇崷鏉挎絻 - 缂佸嫪娆㈤弫鐗堝祦閹恒儱褰涢惃鍕敩閻炲棗婀撮崸鈧?,
    sys_sfc_type int NULL DEFAULT NULL COMMENT '缂佸嫪娆㈢猾璇茬€?- 0:閺傚洣娆? 1:閺傚洦婀? 2:婢舵牠鎽? 3:閺堫剙婀?,
    sys_sfc_content longtext  NULL COMMENT '缂佸嫪娆㈤崘鍛啇 - 瑜版挾琚崹瀣╄礋閺傚洦婀伴弮璺虹摠閸屸晵ue缂佸嫪娆㈡禒锝囩垳',
    sys_sfc_model_cache longtext  NULL COMMENT '濡€虫健缂傛挸鐡?- JSON閺嶇厧绱￠惃鍕贩鐠ф牗膩閸ф鍘ょ純?,
    sys_sfc_delay int NULL DEFAULT NULL COMMENT '瀵ゆ儼绻滈弮鍫曟？(濮ｎ偆顫? - 缂佸嫪娆㈤崝鐘烘祰瀵ゆ儼绻?,
    sys_sfc_version varchar(255)  NULL DEFAULT NULL COMMENT '缂佸嫪娆㈤悧鍫熸拱 - 閻楀牊婀伴崣宄邦洤"1.0.0"',
    sys_sfc_timeout int NULL DEFAULT NULL COMMENT '鐡掑懏妞傞弮鍫曟？(濮ｎ偆顫? - 缂佸嫪娆㈤崝鐘烘祰鐡掑懏妞傜拋鍓х枂',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_sfc_id) USING BTREE,
    INDEX idx_category (sys_sfc_category) USING BTREE COMMENT '閸掑棛琚槐銏犵穿',
    INDEX idx_status (sys_sfc_status) USING BTREE COMMENT '閻樿埖鈧胶鍌ㄥ?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '缂佸嫪娆㈡惔鎾广€?- 鐎涙ê鍋嶉崣顖炲帳缂冾喚娈戦崜宥囶伂缂佸嫪娆㈡穱鈩冧紖' ROW_FORMAT = Dynamic;

-- =====================================================
-- 13. 閻劍鍩涚€瑰顥婄紒鍕鐞?(sys_sfc_install)
-- 閻劑鈧? 鐎涙ê鍋嶉悽銊﹀煕瀹告彃鐣ㄧ憗鍛畱缂佸嫪娆㈤崚妤勩€?-- =====================================================
DROP TABLE IF EXISTS sys_sfc_install;
CREATE TABLE sys_sfc_install (
    sys_sfc_install_id int NOT NULL AUTO_INCREMENT COMMENT '鐎瑰顥婄拋鏉跨秿ID - 娑撳鏁懛顏勵杻',
    user_id int NULL DEFAULT NULL COMMENT '閻劍鍩汭D - 鐎瑰顥婄紒鍕閻ㄥ嫮鏁ら幋?,
    sys_sfc_id int NULL DEFAULT NULL COMMENT '缂佸嫪娆D - 鐞氼偄鐣ㄧ憗鍛畱缂佸嫪娆?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？(鐎瑰顥婇弮鍫曟？)',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_sfc_install_id) USING BTREE,
    INDEX idx_user_id (user_id) USING BTREE COMMENT '閻劍鍩汭D缁便垹绱?,
    INDEX idx_sfc_id (sys_sfc_id) USING BTREE COMMENT '缂佸嫪娆D缁便垹绱?,
    UNIQUE INDEX uk_user_sfc (user_id, sys_sfc_id) USING BTREE COMMENT '閻劍鍩?缂佸嫪娆㈤崬顖欑缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涚€瑰顥婄紒鍕鐞?- 鐎涙ê鍋嶉悽銊﹀煕瀹告彃鐣ㄧ憗鍛畱缂佸嫪娆㈤崚妤勩€? ROW_FORMAT = Dynamic;

-- =====================================================
-- 14. 閻劍鍩涚粭顑跨瑏閺傚湱绮︾€规俺銆?(sys_user_third_party)
-- 閻劑鈧? 鐎涙ê鍋嶉悽銊﹀煕娑撳海顑囨稉澶嬫煙楠炲啿褰撮惃鍕拨鐎规艾鍙х化?-- =====================================================
DROP TABLE IF EXISTS sys_user_third_party;
CREATE TABLE sys_user_third_party  (
    sys_user_third_party_id int NOT NULL AUTO_INCREMENT COMMENT '娑撳鏁璉D',
    sys_user_third_party_type varchar(255)  NULL DEFAULT NULL COMMENT '娑撳鏌熺猾璇茬€烽敍灞筋洤: gitee閵嗕簚x_mini_app 缁?,
    sys_user_third_party_name varchar(255)  NULL DEFAULT NULL COMMENT '缁楊兛绗侀弬閫涗繆閹垰鎮曠粔?,
    sys_user_third_party_value varchar(255)  NULL DEFAULT NULL COMMENT '缁楊兛绗侀弬閫涗繆閹垰鈧?,
    sys_user_id int NULL DEFAULT NULL COMMENT '瑜版挸澧犵化鑽ょ埠閻劍鍩汭D',
    sys_user_third_party_uid varchar(255)  NULL DEFAULT NULL COMMENT '缁楊兛绗侀弬鍦暏閹寸īD',
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_user_third_party_id) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '閻劍鍩涙稉澶嬫煙缂佹垵鐣剧悰?- 鐎涙ê鍋嶉悽銊﹀煕娑撳海顑囨稉澶嬫煙楠炲啿褰撮惃鍕拨鐎规艾鍙х化? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 15. 瀵邦喕淇婇悽銊﹀煕鐞?(sys_wechat_user)
-- 閻劑鈧? 鐎涙ê鍋嶉柅姘崇箖瀵邦喕淇婃担鎾堕兇閹恒儱鍙嗛惃鍕暏閹村嘲鐔€绾偓娣団剝浼?-- =====================================================
DROP TABLE IF EXISTS sys_wechat_user;
CREATE TABLE sys_wechat_user (
    sys_wechat_user_id int NOT NULL AUTO_INCREMENT COMMENT '娑撳鏁璉D - 娑撳鏁懛顏勵杻',
    sys_wechat_open_id varchar(255)  NULL DEFAULT NULL COMMENT '瀵邦喕淇?openid - 瀵邦喕淇婇惂璇茬秿閻ㄥ嚠penID',
    sys_wechat_union_id varchar(255)  NULL DEFAULT NULL COMMENT '瀵邦喕淇?unionid - 瀵邦喕淇婂鈧弨鎯ч挬閸欐壆娈慤nionID',
    sys_wechat_nickname varchar(255)  NULL DEFAULT NULL COMMENT '閺勭數袨 - 瀵邦喕淇婇悽銊﹀煕閺勭數袨',
    sys_wechat_avatar varchar(4000)  NULL DEFAULT NULL COMMENT '婢舵潙鍎?- 瀵邦喕淇婇悽銊﹀煕婢舵潙鍎歎RL',
    sys_user_id int NULL DEFAULT NULL COMMENT '缂佹垵鐣鹃惃鍕兇缂佺喓鏁ら幋绋〥 - 閸忓疇浠堥悽銊﹀煕鐞?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_wechat_user_id) USING BTREE,
    INDEX idx_user_id (sys_user_id) USING BTREE COMMENT '缁崵绮洪悽銊﹀煕ID缁便垹绱?,
    INDEX idx_open_id (sys_wechat_open_id) USING BTREE COMMENT '瀵邦喕淇奜penID缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '瀵邦喕淇婇悽銊﹀煕鐞?- 鐎涙ê鍋嶉柅姘崇箖瀵邦喕淇婃担鎾堕兇閹恒儱鍙嗛惃鍕暏閹村嘲鐔€绾偓娣団剝浼? ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 姒涙顓荤憴鎺曞
-- =====================================================
INSERT INTO sys_role (sys_role_id, sys_role_name, sys_role_code, sys_role_in_system, sys_role_status, sys_role_sort, sys_role_remark, sys_role_hidden) VALUES 
(1, '鐡掑懐楠囩粻锛勬倞閸?, 'SUPER_ADMIN', 1, 1, 1, '缁崵绮虹搾鍛獓缁狅紕鎮婇崨姗堢礉閹枫儲婀侀幍鈧張澶嬫綀闂?, 0),
(2, '缁狅紕鎮婇崨?, 'ADMIN', 1, 1, 2, '缁崵绮虹粻锛勬倞閸?, 0),
(3, '鏉╂劗娣?, 'OPS', 1, 1, 3, '鏉╂劗娣禍鍝勬喅', 0),
(4, '閺咁噣鈧氨鏁ら幋?, 'USER', 0, 1, 4, '閺咁噣鈧氨鏁ら幋?, 0),
(5, '闁劑妫拹鐔荤煑娴?, 'DEPT_LEADER', 1, 1, 5, '鐠愮喕鐭楅柈銊╂，閸欏﹤鐡欓柈銊╂，鐠愶箑褰跨粻锛勬倞', 0),
(6, '濞撶顓?, 'GUEST', 1, 1, 6, '濞撶顓圭拹锕€褰?, 0);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 缁崵绮洪崺铏诡攨閼挎粌宕熸稉搴㈡綀闂?-- 娴犲懍绻氶悾娆忕秼閸?system 鎼存梻鏁ょ€圭偤妾笟婵婄閻?system/common-service 閸╄櫣鍤庨懗钘夊
-- =====================================================
-- 菜单与角色菜单绑定：与线上 system_db 逐行对齐（含任务派发/调度中心/消息渠道/发送日志入口）
INSERT INTO sys_menu (
    sys_menu_id,
    sys_menu_name,
    sys_menu_path,
    sys_menu_i18n,
    sys_menu_title,
    sys_menu_perm,
    sys_menu_pid,
    sys_menu_tree_id,
    sys_menu_component,
    sys_menu_icon,
    sys_menu_type,
    sys_menu_sort,
    sys_menu_hidden,
    sys_menu_show_parent,
    sys_menu_hidden_tag,
    sys_menu_fixed_tag,
    sys_menu_redirect,
    sys_menu_keep_alive,
    create_name,
    create_by,
    create_time,
    update_time,
    update_name,
    update_by,
    sys_menu_role,
    sys_menu_home,
    sys_menu_engine
) VALUES
(1, 'system', '/manage', NULL, '系统管理', NULL, 0, '0,1', '/manage', 'ep:setting', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(2, 'menu', '/manage/menu', NULL, '菜单管理', NULL, 1, '0,1,2', 'system/menu/index', 'ep:menu', 0, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(3, 'setting', '/manage/setting', '', '系统配置', '', 1, '0,1,3', 'system/setting/index', 'ep:setting', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(4, 'role', '/manage/role', NULL, '角色管理', NULL, 1, '0,1,4', 'system/role/index', 'ri:shield-user-line', 0, 2, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(5, '', '', '', '新增', 'sys:role:add', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(6, '', '', '', '删除', 'sys:role:delete', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(7, '', '', '', '修改', 'sys:role:update', 4, '0,1,4', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(8, 'user', '/manage/user', '', '用户管理', '', 1, '0,1,8', 'system/user/index', 'ri:user-settings-line', 0, 3, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(9, 'userLog', '/manage/log/login', '', '登录日志', '', 1, '0,1,9', 'system/userlog/index', 'ep:alarm-clock', 0, 998, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(11, 'dept', '/manage/dept', '', '机构管理', '', 1, '0,1,11', 'system/dept/index', 'ep:star', 0, 5, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(12, '', '', '', '删除', 'sys:login:delete', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(13, '', '', '', '编辑', 'sys:login:update', 8, '0,1,8', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(14, '', '', '', '新增', 'sys:login:add', 8, '0,1,8,14', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(15, 'dict', '/manage/dict', '', '字典管理', '', 1, '0,1,15', 'system/dict/index', 'humbleicons:dashboard', 0, 6, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(18, 'sysLog', '/manage/syslog', '', '系统日志', '', 1, '0,1,18', 'system/syslog/index', 'ep:list', 0, 999, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(19, '', '', '', '删除', 'sys:log:delete', 18, '0,1,18,19', '', '', 3, 1, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), NULL, NULL, '', NULL, NULL),
(20, 'online', '/manage/online', '', '在线用户', '', 1, '0,1,20', 'system/online/index', 'ep:monitor', 0, 7, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(30, 'post', '/manage/post', NULL, '岗位管理', NULL, 1, '0,1,30', 'system/post/index', 'ri:briefcase-4-line', 0, 4, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(31, 'tenant', '/manage/tenant', NULL, '租户管理', NULL, 1, '0,1,31', 'system/tenant/index', 'ri:community-line', 0, 8, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(32, 'announcement', '/manage/announcement', NULL, '通知公告', NULL, 1, '0,1,32', 'system/announcement/index', 'ep:bell', 0, 9, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(33, 'feedback', '/manage/feedback', NULL, '意见反馈', NULL, 1, '0,1,33', 'system/feedback/index', 'ep:chat-dot-round', 0, 10, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(34, 'ip', '/manage/ip', NULL, 'IP 黑白名单', NULL, 1, '0,1,34', 'system/ip/index', 'ep:position', 0, 11, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(35, 'cache', '/manage/cache', NULL, '缓存监控', NULL, 53, '0,53,35', 'system/cache/index', 'ep:coin', 0, 12, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(36, 'errorlog', '/manage/errorlog', NULL, '错误日志', NULL, 1, '0,1,36', 'system/errorlog/index', 'ep:warning', 0, 13, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(37, 'message', '/manage/message', NULL, '站内消息', NULL, 51, '0,51,37', 'system/message/index', 'ep:message', 0, 14, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(38, 'monitor', '/manage/monitor', NULL, '服务监控', NULL, 53, '0,53,38', 'system/monitor/index', 'ep:odometer', 0, 15, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(50, 'filesystem', '/filesystem', NULL, '文件系统', NULL, 0, '0,50', '/filesystem', 'ep:folder', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(51, 'msgcenter', '/message', NULL, '消息中心', NULL, 0, '0,51', '/message', 'ep:chat-dot-square', 0, 24, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(52, 'taskcenter', '/task', NULL, '任务调度', NULL, 0, '0,52', '/task', 'ep:timer', 0, 25, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(53, 'monitorcenter', '/monitor', NULL, '运维监控', NULL, 0, '0,53', '/monitor', 'ep:odometer', 0, 12, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, '', NULL, NULL),
(39, 'file', '/manage/file', NULL, '文件管理', NULL, 50, '0,50,39', 'filesystem/list/index', 'ep:folder', 0, 20, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(40, 'filegroup', '/manage/filegroup', NULL, '文件分组', NULL, 50, '0,50,40', 'filesystem/group/index', 'ep:files', 0, 21, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(41, 'filestorage', '/manage/filestorage', NULL, '存储配置', NULL, 50, '0,50,41', 'filesystem/filestorage/index', 'ep:box', 0, 22, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(42, 'filesetting', '/manage/filesetting', NULL, '文件设置', NULL, 50, '0,50,42', 'filesystem/setting/index', 'ep:setting', 0, 23, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(43, 'msgtemplate', '/manage/msgtemplate', NULL, '消息模板', NULL, 51, '0,51,43', 'message/template/index', 'ep:document', 0, 24, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(44, 'taskdispatch', '/manage/taskdispatch', '', '任务派发', '', 52, '0,52,44', 'task/dispatch/index', 'ep:promotion', 0, 25, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(45, 'scheduler', '/manage/scheduler', '', '调度中心', '', 52, '0,52,45', 'task/scheduler/index', 'ep:timer', 0, 26, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(46, 'msgprovider', '/manage/msgprovider', '', '消息渠道', '', 51, '0,51,46', 'message/provider/index', 'ep:connection', 0, 27, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL),
(47, 'msglog', '/manage/msglog', '', '发送日志', '', 51, '0,51,47', 'message/log/index', 'ep:tickets', 0, 28, 0, 1, 0, 0, '', 0, 'sa', 1, NOW(), NOW(), 'sa', 1, 'SUPER_ADMIN,ADMIN', NULL, NULL);

INSERT INTO sys_role_menu (sys_role_menu_id, sys_role_id, sys_menu_id) VALUES
(82, 2, 4),
(83, 2, 5),
(84, 2, 6),
(85, 2, 7),
(86, 2, 8),
(87, 2, 12),
(88, 2, 13),
(89, 2, 14),
(90, 2, 1),
(91, 2, 2),
(92, 2, 11),
(93, 2, 15),
(96, 2, 9),
(97, 2, 3),
(131, 3, 2),
(132, 3, 4),
(133, 3, 5),
(134, 3, 6),
(135, 3, 7),
(136, 3, 8),
(137, 3, 12),
(138, 3, 13),
(139, 3, 14),
(140, 3, 9),
(141, 3, 11),
(142, 3, 15),
(143, 3, 1),
(161, 2, 18),
(162, 2, 19),
(163, 2, 43),
(164, 2, 42),
(165, 2, 41),
(166, 2, 40),
(167, 2, 39),
(168, 2, 38),
(169, 2, 37),
(170, 2, 36),
(171, 2, 35),
(172, 2, 34),
(173, 2, 33),
(174, 2, 32),
(175, 2, 31),
(176, 2, 30),
(177, 2, 44),
(178, 2, 45),
(179, 2, 46),
(180, 2, 47),
(181, 2, 50),
(182, 2, 51),
(183, 2, 52),
(184, 2, 53);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 姒涙顓荤化鑽ょ埠鐠愶箑褰?-- 鐠愶箑褰? sa / admin / ops
-- 鐎靛棛鐖? 123456 (閸撳秶顏崗?MD5閿涘苯鍟€閻㈠崬鎮楃粩顖涘⒔鐞?MD5(md5(password)+salt))
-- =====================================================
INSERT INTO sys_user (sys_user_id, sys_user_username, sys_user_nickname, sys_user_password, sys_user_salt, sys_user_delete, sys_user_status, sys_user_in_system) VALUES
(1, 'sa', '鐡掑懐楠囩粻锛勬倞閸?, 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1),
(2, 'admin', '缁狅紕鎮婇崨?, 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1),
(3, 'ops', '鏉╂劗娣?, 'ea48576f30be1669971699c09ad05c94', '123456', 0, 1, 1);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 姒涙顓荤拹锕€褰跨憴鎺曞閸忓疇浠?-- =====================================================
INSERT INTO sys_user_role (sys_user_id, sys_role_id) VALUES
(1, 1),
(2, 2),
(3, 3);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 姒涙顓荤紒鍕矏閺堢儤鐎?-- =====================================================
INSERT INTO sys_dept (sys_dept_id, sys_dept_name, sys_dept_pid, sys_dept_tree_id, sys_dept_code, sys_dept_sort, sys_dept_status) VALUES 
(1, '閹鍎?, 0, '0,1', 'ROOT', 1, 0);


-- ============================================
-- 11. 缁崵绮洪崗顒€鎲＄悰?(sys_announcement)
-- ============================================
DROP TABLE IF EXISTS sys_announcement;
CREATE TABLE sys_announcement (
    sys_announcement_id int NOT NULL AUTO_INCREMENT COMMENT '閸忣剙鎲D - 娑撳鏁懛顏勵杻',
    sys_announcement_title varchar(200)  NOT NULL COMMENT '閸忣剙鎲￠弽鍥暯',
    sys_announcement_content text  NULL COMMENT '閸忣剙鎲￠崘鍛啇(HTML) - 閺€顖涘瘮鐎靛本鏋冮張?,
    sys_announcement_level varchar(20)  NULL DEFAULT 'info' COMMENT '缁狙冨焼 - info:娣団剝浼?warning:鐠€锕€鎲?important:闁插秷顩?emergency:缁毖勨偓?,
    sys_announcement_position varchar(20)  NULL DEFAULT 'default' COMMENT '鐏炴洜銇氭担宥囩枂 - default:Tabs娑撳鏌?fixed-top:閸ュ搫鐣炬い鍫曞劥',
    sys_announcement_type varchar(20)  NULL DEFAULT 'global' COMMENT '缁鐎?- global:閸忋劌鐪?role:閹稿洤鐣剧憴鎺曞',
    sys_announcement_target_roles varchar(1000)  NULL DEFAULT NULL COMMENT '閻╊喗鐖ｇ憴鎺曞ID閸掓銆?JSON閺佹壆绮? - type=role閺冨墎鏁撻弫?,
    sys_announcement_status int NULL DEFAULT 0 COMMENT '閻樿埖鈧?- 0:閼藉顭?1:瀹告彃褰傜敮?2:瀹歌弓绗呴弸?,
    sys_announcement_sort int NULL DEFAULT 0 COMMENT '閹烘帒绨弶鍐櫢 - 鐡掑﹤銇囩搾濠囨浆閸?,
    sys_announcement_publish_time datetime NULL DEFAULT NULL COMMENT '閸欐垵绔烽弮鍫曟？',
    sys_announcement_expire_time datetime NULL DEFAULT NULL COMMENT '鏉╁洦婀￠弮鍫曟？ - 缁?濮橀晲绗夋潻鍥ㄦ埂',
    sys_announcement_target_url varchar(500)  NULL DEFAULT NULL COMMENT '鐠哄疇娴嗛柧鐐复 - 閻愮懓鍤崗顒€鎲＄捄瀹犳祮',
    sys_announcement_publisher_name varchar(100)  NULL DEFAULT NULL COMMENT '閸欐垵绔锋禍?,
    create_name varchar(255)  NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int NULL DEFAULT NULL COMMENT '閸掓稑缂撴禍绡扗',
    create_time datetime NULL DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime NULL DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255)  NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int NULL DEFAULT NULL COMMENT '閺囧瓨鏌婃禍绡扗',
    PRIMARY KEY (sys_announcement_id) USING BTREE,
    INDEX idx_announcement_status (sys_announcement_status) USING BTREE COMMENT '閸忣剙鎲￠悩鑸碘偓浣哄偍瀵?,
    INDEX idx_announcement_publish_time (sys_announcement_publish_time) USING BTREE COMMENT '閸欐垵绔烽弮鍫曟？缁便垹绱?
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4  COMMENT = '缁崵绮洪崗顒€鎲＄悰?- 鐎涙ê鍋嶇化鑽ょ埠閸忣剙鎲￠崪宀勨偓姘辩叀' ROW_FORMAT = DYNAMIC;

-- =====================================================
-- 12. 婢舵潙鍎氬鍡楃暰娑斿銆?(sys_avatar_frame)
-- 閻劑鈧? 鐎涙ê鍋嶆径鏉戝剼濡楀棗鐣炬稊澶涚礄濞撴劕褰夊?id=1-22閵嗕礁娴橀悧鍥攱 id=23-28閵嗕胶鏁撻弮銉︻攱 id=100閿?
-- =====================================================
DROP TABLE IF EXISTS sys_avatar_frame;
CREATE TABLE sys_avatar_frame (
    sys_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '婢舵潙鍎氬鍜篋',
    sys_avatar_frame_name varchar(100) NOT NULL COMMENT '婢舵潙鍎氬鍡楁倳缁?,
    sys_avatar_frame_type varchar(50) DEFAULT 'base' COMMENT '缁鐎? base-閸╄櫣顢?birthday-閻㈢喐妫?event-濞茶濮?,
    sys_avatar_frame_image varchar(500) DEFAULT NULL COMMENT '婢舵潙鍎氬鍡楁禈閻楀槮RL',
    sys_avatar_frame_color varchar(200) DEFAULT NULL COMMENT '濞撴劕褰夐懗灞炬珯閼?css)',
    sys_avatar_frame_desc varchar(500) DEFAULT NULL COMMENT '閹诲繗鍫?,
    sys_avatar_frame_acquire varchar(50) DEFAULT 'default' COMMENT '閼惧嘲褰囬柅鏂跨窞: default-姒涙顓?birthday-閻㈢喐妫?achievement-閹存劕姘?,
    sys_avatar_frame_sort int DEFAULT 0 COMMENT '閹烘帒绨?,
    sys_avatar_frame_status int DEFAULT 1 COMMENT '閻樿埖鈧? 0-缁備胶鏁?1-閸氼垳鏁?,
    create_name varchar(255) DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int DEFAULT NULL COMMENT '閸掓稑缂撴禍?,
    create_time datetime DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255) DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int DEFAULT NULL COMMENT '閺囧瓨鏌婃禍?,
    PRIMARY KEY (sys_avatar_frame_id) USING BTREE,
    KEY idx_type (sys_avatar_frame_type) USING BTREE,
    KEY idx_status (sys_avatar_frame_status) USING BTREE
) ;

-- =====================================================
-- 13. 閻劍鍩涙径鏉戝剼濡楀棗鍙ч懕鏃囥€?(sys_user_avatar_frame)
-- 閻劑鈧? 鐠佹澘缍嶉悽銊﹀煕瀹歌尪骞忛崣鏍畱婢舵潙鍎氬?
-- =====================================================
DROP TABLE IF EXISTS sys_user_avatar_frame;
CREATE TABLE sys_user_avatar_frame (
    sys_user_avatar_frame_id int NOT NULL AUTO_INCREMENT COMMENT '閻劍鍩涙径鏉戝剼濡楀捄D',
    sys_user_id int NOT NULL COMMENT '閻劍鍩汭D',
    sys_avatar_frame_id int NOT NULL COMMENT '婢舵潙鍎氬鍜篋',
    acquire_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '閼惧嘲褰囬弮鍫曟？',
    expire_time datetime DEFAULT NULL COMMENT '鏉╁洦婀￠弮鍫曟？(NULL=濮橀晲绠?',
    create_name varchar(255) DEFAULT NULL COMMENT '閸掓稑缂撴禍鍝勵潣閸?,
    create_by int DEFAULT NULL COMMENT '閸掓稑缂撴禍?,
    create_time datetime DEFAULT NULL COMMENT '閸掓稑缂撻弮鍫曟？',
    update_time datetime DEFAULT NULL COMMENT '閺囧瓨鏌婇弮鍫曟？',
    update_name varchar(255) DEFAULT NULL COMMENT '閺囧瓨鏌婃禍鍝勵潣閸?,
    update_by int DEFAULT NULL COMMENT '閺囧瓨鏌婃禍?,
    PRIMARY KEY (sys_user_avatar_frame_id) USING BTREE,
    UNIQUE KEY uk_user_frame (sys_user_id, sys_avatar_frame_id) USING BTREE,
    KEY idx_user_id (sys_user_id) USING BTREE,
    KEY idx_frame_id (sys_avatar_frame_id) USING BTREE
) ;

-- =====================================================
-- 头像框种子 - 「不佩戴」(1)、常规 (2-7)、生肖 (201-212)、神兽 (221-226)、生日框 (100)
-- =====================================================
INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_color, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort) VALUES
(1, '经典', 'base', 'transparent', '默认无边框', 'default', 0);

-- 常规框：只有渐变色，前端按「渐变 + 径向遮罩挖孔」画素环
INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_color, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort) VALUES
(2, '素环', 'regular', 'linear-gradient(135deg, #f2f5f8 0%, #b9c2cc 35%, #ffffff 55%, #8e97a1 100%)', '常规款·银白素环', 'default', 20),
(3, '墨玉', 'regular', 'linear-gradient(135deg, #3a3f47 0%, #6b7480 35%, #1b1e23 60%, #59616c 100%)', '常规款·墨玉深灰', 'default', 21),
(4, '鎏金', 'regular', 'linear-gradient(135deg, #fff3c4 0%, #d4a531 30%, #a9772a 55%, #f7e59b 100%)', '常规款·鎏金暖阳', 'default', 22),
(5, '青瓷', 'regular', 'linear-gradient(135deg, #e2f5ee 0%, #6fbfa9 35%, #38796d 65%, #bfe6d8 100%)', '常规款·青瓷淡翠', 'default', 23),
(6, '绛朱', 'regular', 'linear-gradient(135deg, #ffd9c9 0%, #d9534a 35%, #9b241c 65%, #ffb3a0 100%)', '常规款·绛朱丹霞', 'default', 24),
(7, '星辉', 'regular', 'linear-gradient(135deg, #dfe4ff 0%, #7c6ff0 30%, #34296e 60%, #a8b6ff 100%)', '常规款·星辉蓝紫', 'default', 25);

INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_image, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort) VALUES
(201, '子鼠', 'zodiac', '/assets/avatar-frames/zodiac-rat.svg', '暗珐琅描金·生肖鼠', 'default', 1),
(202, '丑牛', 'zodiac', '/assets/avatar-frames/zodiac-ox.svg', '暗珐琅描金·生肖牛', 'default', 2),
(203, '寅虎', 'zodiac', '/assets/avatar-frames/zodiac-tiger.svg', '暗珐琅描金·生肖虎', 'default', 3),
(204, '卯兔', 'zodiac', '/assets/avatar-frames/zodiac-rabbit.svg', '暗珐琅描金·生肖兔', 'default', 4),
(205, '辰龙', 'zodiac', '/assets/avatar-frames/zodiac-dragon.svg', '暗珐琅描金·生肖龙', 'default', 5),
(206, '巳蛇', 'zodiac', '/assets/avatar-frames/zodiac-snake.svg', '暗珐琅描金·生肖蛇', 'default', 6),
(207, '午马', 'zodiac', '/assets/avatar-frames/zodiac-horse.svg', '暗珐琅描金·生肖马', 'default', 7),
(208, '未羊', 'zodiac', '/assets/avatar-frames/zodiac-goat.svg', '暗珐琅描金·生肖羊', 'default', 8),
(209, '申猴', 'zodiac', '/assets/avatar-frames/zodiac-monkey.svg', '暗珐琅描金·生肖猴', 'default', 9),
(210, '酉鸡', 'zodiac', '/assets/avatar-frames/zodiac-rooster.svg', '暗珐琅描金·生肖鸡', 'default', 10),
(211, '戌狗', 'zodiac', '/assets/avatar-frames/zodiac-dog.svg', '暗珐琅描金·生肖狗', 'default', 11),
(212, '亥猪', 'zodiac', '/assets/avatar-frames/zodiac-pig.svg', '暗珐琅描金·生肖猪', 'default', 12),
(221, '狮鹫', 'beast', '/assets/avatar-frames/beast-griffin.svg', '暗珐琅描金·西方神兽狮鹫', 'limit', 13),
(222, '独角兽', 'beast', '/assets/avatar-frames/beast-unicorn.svg', '暗珐琅描金·西方神兽独角兽', 'limit', 14),
(223, '凤凰', 'beast', '/assets/avatar-frames/beast-phoenix.svg', '暗珐琅描金·神兽凤凰', 'limit', 15),
(224, '麒麟', 'beast', '/assets/avatar-frames/beast-qilin.svg', '暗珐琅描金·神兽麒麟', 'limit', 16),
(225, '九尾狐', 'beast', '/assets/avatar-frames/beast-fox.svg', '暗珐琅描金·神兽九尾狐', 'limit', 17),
(226, '天马', 'beast', '/assets/avatar-frames/beast-pegasus.svg', '暗珐琅描金·西方神兽天马', 'limit', 18);

INSERT INTO sys_avatar_frame (sys_avatar_frame_id, sys_avatar_frame_name, sys_avatar_frame_type, sys_avatar_frame_image, sys_avatar_frame_desc, sys_avatar_frame_acquire, sys_avatar_frame_sort, sys_avatar_frame_status) VALUES
(100, '生日快乐', 'birthday', '/assets/avatar-frames/birthday.svg', '生日专属限定头像框', 'birthday', 99, 1);


-- =====================================================
-- Service 濡€虫健閸掓繂顫愰崠鏍ㄦ殶閹诡喛鍓奸張?-- 閻楀牊婀? V1.0
-- 鐠囧瓨妲? 閸栧懎鎯堢化鑽ょ埠鐎涙鍚€閵嗕礁鐡ч崗鎼併€嶉妴浣洪兇缂佺喖鍘ょ純顔衡偓浣规瀮娴犲墎閮寸紒鐔峰瀻缂佸嫮鐡戦崺铏诡攨閺佺増宓?-- 閹笛嗩攽閺傜懓绱? 瀵倹顒為幍褑顢戦敍鍫ｆ珓閹风喓鍤庣粙瀣剁礆
-- =====================================================



-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 鐎涙鍚€閺佺増宓?-- =====================================================
INSERT INTO sys_dict VALUES (1, '閸樺倸顔?, 'CHANG_JIA', 0, 1, 0, '', NULL, '娑撳鏌熼崢鍌氼啀', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (2, '濡剝婢樼猾璇茬€?, 'MU_BAN_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (3, '閻厺淇?, 'DUAN_XIN', 2, 1, 0, '', NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict VALUES (4, '瀵邦喕淇?, 'WEI_XIN', 2, 1, 0, NULL, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (5, '缂佸嫪娆㈤崝鐔诲厴', 'ZU_JIAN_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (6, '妞ゅ湱娲伴崝鐔诲厴', 'XIANG_MU_GONG_NENG', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict VALUES (7, '鐠佹儳顦猾璇茬€?, 'SHE_BEI_LEI_XING', 0, 1, 0, '', NULL, '', 'sa', 1, NOW(), NULL, NULL, NULL);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 鐎涙鍚€妞よ鏆熼幑?-- =====================================================
INSERT INTO sys_dict_item VALUES (1, 1, '濞村嘲鎮嶆禍鎴炴磾', 'HAI_KANG_YUN_YAO', NULL, 1, 1, '', 0, NULL, '濞村嘲鎮嶆禍鎴炴磾', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (2, 1, '闂冨潡鍣锋禍?, 'ALIYUN', NULL, 1, 1, NULL, 0, NULL, '闂冨潡鍣锋禍?, 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (3, 1, '閻ф儳瀹虫禍?, 'BDYUN', NULL, 1, 1, NULL, 0, NULL, '閻ф儳瀹虫禍?, 'sa', NULL, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (4, 2, '閻厺淇?, 'SMS', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (5, 3, '閻厺淇婇柅姘辩叀', 'DUAN_XIN_TONG_ZHI', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (6, 2, '缁旀瑥鍞?, 'WEB', NULL, 1, 2, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (7, 2, '瀵邦喕淇?, 'WECHAT', NULL, 1, 2, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (8, 3, '妤犲矁鐦夐惍?, 'YAN_ZHENG_MA', NULL, 1, 1, '', 0, NULL, '', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_dict_item VALUES (9, 4, '瀵邦喕淇婇柅姘辩叀', 'WEI_XIN_TONG_ZHI', NULL, 1, NULL, NULL, 0, NULL, NULL, 'sa', 1, NOW(), NULL, NULL, NULL);
INSERT INTO sys_dict_item VALUES (10, 1, '鐠侇垶顥?, 'XUN_FEI', NULL, 1, 1, '', 0, 'sysProjectAppId,sysProjectAppSecret', '', 'sa', 1, NOW(), NOW(), 'sa', 1);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 缁崵绮洪柊宥囩枂
-- =====================================================
INSERT INTO sys_setting_group VALUES (1, '姒涙顓婚柊宥囩枂', 'default', 'ep:setting', 1, '缁崵绮烘妯款吇闁板秶鐤?, 1, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (2, '缁崵绮洪柊宥囩枂', 'config', 'ep:tools', 1, '缁崵绮洪崺铏诡攨鏉╂劘顢戦柊宥囩枂', 2, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (3, '閸楁洜鍋ｉ惂璇茬秿', 'sso', 'ri:shield-user-line', 1, '缁楊兛绗侀弬鍦瑜版洘鈧绱戦崗?, 3, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (4, 'Gitee閻ц缍?, 'gitee', 'simple-icons:gitee', 1, 'Gitee OAuth闁板秶鐤?, 4, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (5, 'GitHub閻ц缍?, 'github', 'simple-icons:github', 1, 'GitHub OAuth闁板秶鐤?, 5, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting_group VALUES (6, 'Google閻ц缍?, 'google', 'simple-icons:google', 1, 'Google OAuth闁板秶鐤?, 6, 0, 'sa', 1, NOW(), NOW(), 'sa', 1);

INSERT INTO sys_setting VALUES (1, 'CheckCodeOpen', '', 'false', 'Boolean', 'default', 1, 4, 0, 1, 0, NULL, 15, NULL, '妤犲矁鐦夐惍浣哥磻閸?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (2, 'SystemName', '', '閸氬骸褰寸粻锛勬倞缁崵绮?, 'String', 'default', 1, 0, 0, 1, 0, NULL, 15, NULL, '缁崵绮洪弽鍥暯', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (3, 'SlidingBlockOpen', '', 'true', 'Boolean', 'default', 1, 5, 0, 1, 0, NULL, 15, NULL, '濠婃垵濮╂宀冪槈閻?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (17, 'OpenSmsLogin', '', 'true', 'Boolean', 'default', 1, 6, 0, 1, 0, NULL, 15, NULL, '閻厺淇婇惂璇茬秿瀵偓閸?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (30, 'CheckTotpOpen', 'login.otp', 'false', 'Boolean', 'default', 1, 7, 0, 1, 0, NULL, 15, NULL, 'OTP閸斻劍鈧礁褰涙禒?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (31, 'BrandName', 'login.brandName', '閸氬骸褰寸粻锛勬倞缁崵绮?, 'String', 'default', 1, 1, 0, 1, 0, NULL, 15, NULL, '閸濅胶澧濋崥宥囆?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (32, 'Welcome', 'login.welcome', '濞嗐垼绻嬮惂璇茬秿', 'String', 'default', 1, 2, 0, 1, 0, NULL, 15, NULL, '濞嗐垼绻嬬拠?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (33, 'Description', 'login.description', '缁崵绮虹粻锛勬倞楠炲啿褰?, 'String', 'default', 1, 3, 0, 1, 0, NULL, 15, NULL, '缁崵绮洪幓蹇氬牚', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (34, 'LogoUrl', 'login.logoUrl', '/assets/logo.png', 'String', 'default', 1, 8, 0, 1, 0, NULL, 15, NULL, '閸濅胶澧滾ogo', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (35, 'ImageUrl', 'login.imageUrl', '/assets/login/science.png', 'String', 'default', 1, 9, 0, 1, 0, NULL, 15, NULL, '娑撴槒顫嬬憴澶婃禈閻?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (36, 'AvatarUrl', 'login.avatarUrl', '', 'String', 'default', 1, 10, 0, 1, 0, NULL, 15, NULL, '姒涙顓绘径鏉戝剼', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (37, 'BeiAnHao', 'login.beiAnHao', '', 'String', 'default', 1, 11, 0, 1, 0, NULL, 15, NULL, '婢跺洦顢嶉崣?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (38, 'FeatureTags', 'login.featureTags', '[{"icon":"DataAnalysis","text":"妤傛ɑ鏅ョ粻锛勬倞"},{"icon":"Brush","text":"閻滈鍞拋鎹愵吀"},{"icon":"Lock","text":"鐎瑰鍙忛崣顖炴浆"}]', 'String', 'default', 1, 12, 0, 1, 0, NULL, 15, NULL, '閸旂喕鍏橀悧瑙勨偓褎鐖ｇ粵?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting (`sys_setting_id`, `sys_setting_name`, `sys_setting_code`, `sys_setting_value`, `sys_setting_value_type`, `sys_setting_group`, `sys_setting_status`, `sys_setting_sort`, `sys_setting_collect`, `sys_setting_in_system`, `sys_setting_app_inner`, `sys_setting_admin`, `sys_setting_version`, `sys_setting_remark`, `create_name`, `create_time`) VALUES (39, 'AvatarFrameOpen', 'other.avatarFrame.enable', 'true', 'Boolean', 'default', 1, 13, 0, 1, 0, NULL, 15, '头像框能力开关', 'sa', NOW());
INSERT INTO sys_setting (`sys_setting_id`, `sys_setting_name`, `sys_setting_code`, `sys_setting_value`, `sys_setting_value_type`, `sys_setting_group`, `sys_setting_status`, `sys_setting_sort`, `sys_setting_collect`, `sys_setting_in_system`, `sys_setting_app_inner`, `sys_setting_admin`, `sys_setting_version`, `sys_setting_remark`, `create_name`, `create_time`) VALUES (40, 'PersonalConfigOpen', 'other.personalConfig.enable', 'true', 'Boolean', 'default', 1, 14, 0, 1, 0, NULL, 15, '个性配置能力开关', 'sa', NOW());
INSERT INTO sys_setting VALUES (5, 'CodecResponseOpen', '', 'false', 'Boolean', 'config', 1, 10, 0, 1, 0, NULL, 26, NULL, '鏉╂柨娲栭崝鐘茬槕', 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (6, 'WatermarkOpen', '', 'false', 'Boolean', 'config', 1, 5, 0, 1, 0, NULL, 26, NULL, '濮樻潙宓?, 'sa', NULL, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (8, 'Version', '', '1', 'Number', 'config', 1, 4, 0, 1, 0, NULL, 123, 'fe7db08a-1001-4e7b-95ab-776a569a85c0', '缁崵绮洪悧鍫熸拱', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (9, 'SocketUrl', '', 'http://127.0.0.1:29181', 'Array', 'config', 1, 2, 0, 1, 0, 1, 26, NULL, 'socket.io閺堝秴濮熼崳銊ユ勾閸р偓', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (10, 'SocketOpen', '', 'false', 'Boolean', 'config', 1, 12, 0, 1, 0, 1, 26, NULL, 'socket.io閺勵垰鎯佸鈧崥?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (11, 'SocketPath', '', '/socket.io', 'String', 'config', 1, 13, 0, 1, 0, NULL, NULL, NULL, 'socket.io鐠侯垰绶?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (12, 'WatermarkColor', '', '#409EFF', 'Color', 'config', 1, 6, 0, 1, 0, NULL, 26, NULL, '濮樻潙宓冩０婊嗗', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (13, 'WatermarkAlpha', '', '0.35', 'Number', 'config', 1, 8, 0, 1, 0, NULL, 26, NULL, '濮樻潙宓冮柅蹇旀鎼?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (14, 'WatermarkRotate', '', '-30', 'Number', 'config', 1, 7, 0, 1, 0, NULL, 26, NULL, '濮樻潙宓冪憴鎺戝', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (15, 'CodecRequestOpen', '', 'false', 'Boolean', 'config', 1, 14, 0, 1, 0, NULL, 26, NULL, '鐠囬攱鐪伴崝鐘茬槕', NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (16, 'CodecRequestKey', '', '0123456789abcdef', 'String', 'config', 1, 3, 0, 1, 1, NULL, 26, NULL, '鐠囬攱鐪伴崝鐘茬槕鐎靛棝鎸?, NULL, NULL, NULL, NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (18, 'Gitee', '', 'false', 'Boolean', 'sso', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee閻ц缍嶅鈧崗?, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (19, 'Github', '', 'false', 'Boolean', 'sso', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub閻ц缍嶅鈧崗?, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (20, 'Google', '', 'false', 'Boolean', 'sso', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google閻ц缍嶅鈧崗?, 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (21, 'clientId', '', '', 'String', 'gitee', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Gitee Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (22, 'clientSecret', '', '', 'Password', 'gitee', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Gitee Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (23, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/gitee/callback', 'String', 'gitee', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Gitee閸ョ偠鐨熼崷鏉挎絻', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (24, 'clientId', '', '', 'String', 'github', 1, 1, 0, 1, 0, NULL, 1, NULL, 'GitHub Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (25, 'clientSecret', '', '', 'Password', 'github', 1, 2, 0, 1, 0, NULL, 1, NULL, 'GitHub Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (26, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/github/callback', 'String', 'github', 1, 3, 0, 1, 0, NULL, 1, NULL, 'GitHub閸ョ偠鐨熼崷鏉挎絻', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (27, 'clientId', '', '', 'String', 'google', 1, 1, 0, 1, 0, NULL, 1, NULL, 'Google Client ID', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (28, 'clientSecret', '', '', 'Password', 'google', 1, 2, 0, 1, 0, NULL, 1, NULL, 'Google Client Secret', 'sa', 1, NOW(), NOW(), 'sa', 1);
INSERT INTO sys_setting VALUES (29, 'redirectUri', '', 'http://127.0.0.1:18170/system/api/v2/third/login/google/callback', 'String', 'google', 1, 3, 0, 1, 0, NULL, 1, NULL, 'Google閸ョ偠鐨熼崷鏉挎絻', 'sa', 1, NOW(), NOW(), 'sa', 1);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 閺傚洣娆㈢化鑽ょ埠閸掑棛绮?-- =====================================================
INSERT INTO sys_file_system_group VALUES (1, '姒涙顓婚崚鍡欑矋', 0, 'default', '/default', 1, 'default', NULL, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (2, '閸ュ墽澧?, 0, 'images', '/images', 1, 'images', NULL, 0, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (3, '鐟欏棝顣?, 0, 'videos', '/videos', 1, 'videos', NULL, 0, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (4, '閺傚洦銆?, 0, 'documents', '/documents', 1, 'documents', NULL, 0, 4, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO sys_file_system_group VALUES (5, '闂婃娊顣?, 0, 'audios', '/audios', 1, 'audios', NULL, 0, 5, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- =====================================================
-- 閸掓繂顫愰弫鐗堝祦 - 閺傚洣娆㈢化鑽ょ埠闁板秶鐤?-- =====================================================
INSERT INTO sys_file_system_setting VALUES (1, '姒涙顓婚柊宥囩枂', 1, 100, 1, 10, 1, 0, 8888, '0.0.0.0', '/file', 1, 1, NULL, NULL, 1024, NULL, NULL, 0, 1, 'local', NULL, NULL, NULL, NULL, NULL, NULL, NULL);






-- ===== AI H2 鍏煎 schema (杩藉姞) =====
-- Minimal H2-compatible schema for local AI-starter test (H2 MySQL mode)
-- Stripped of MySQL-only procedural syntax; plain CREATE TABLE only.

CREATE TABLE IF NOT EXISTS sys_ai_bot_binding (
  sys_ai_bot_binding_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_bot_binding_user_id BIGINT,
  sys_ai_bot_binding_owner_key VARCHAR(128),
  sys_ai_bot_binding_bot_id VARCHAR(128),
  sys_ai_bot_binding_bot_token VARCHAR(512),
  sys_ai_bot_binding_platform VARCHAR(32),
  sys_ai_bot_binding_nickname VARCHAR(128),
  sys_ai_bot_binding_avatar_url VARCHAR(512),
  sys_ai_bot_binding_gateway_port INT,
  sys_ai_bot_binding_status VARCHAR(32) DEFAULT 'offline',
  sys_ai_bot_binding_bind_source VARCHAR(32),
  sys_ai_bot_binding_model VARCHAR(128),
  sys_ai_bot_binding_thinking TINYINT DEFAULT 0,
  sys_ai_bot_binding_thinking_effort VARCHAR(16),
  sys_ai_bot_binding_system_prompt CLOB,
  sys_ai_bot_binding_record_usage TINYINT DEFAULT 1,
  sys_ai_bot_binding_context_thinking TINYINT,
  sys_ai_bot_binding_context_size INT,
  sys_ai_bot_binding_history_size INT,
  sys_ai_bot_binding_command_enabled TINYINT,
  sys_ai_bot_binding_last_active_time TIMESTAMP,
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_global_setting (
  sys_ai_global_setting_id INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_global_setting_group VARCHAR(64) DEFAULT 'default',
  sys_ai_global_setting_field VARCHAR(128),
  sys_ai_global_setting_value CLOB,
  sys_ai_global_setting_value_type VARCHAR(32) DEFAULT 'STRING',
  sys_ai_global_setting_description VARCHAR(512),
  sys_ai_global_setting_sort INT DEFAULT 0,
  sys_ai_global_setting_status INT DEFAULT 1,
  create_name VARCHAR(255),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(255),
  update_by INT
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ai_global_setting_field ON sys_ai_global_setting(sys_ai_global_setting_group, sys_ai_global_setting_field);

CREATE TABLE IF NOT EXISTS sys_ai_usage (
  sys_ai_usage_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_usage_group_name VARCHAR(32),
  sys_ai_usage_provider VARCHAR(64),
  sys_ai_usage_model VARCHAR(128),
  sys_ai_usage_call_type VARCHAR(16),
  sys_ai_usage_prompt_tokens INT,
  sys_ai_usage_completion_tokens INT,
  sys_ai_usage_total_tokens INT,
  sys_ai_usage_input_cost DOUBLE,
  sys_ai_usage_output_cost DOUBLE,
  sys_ai_usage_total_cost DOUBLE,
  sys_ai_usage_request_id VARCHAR(128),
  sys_ai_usage_user_id VARCHAR(64),
  sys_ai_usage_call_time TIMESTAMP,
  sys_ai_usage_inference_strength VARCHAR(32),
  sys_ai_usage_endpoint VARCHAR(128),
  sys_ai_usage_ip VARCHAR(64),
  sys_ai_usage_type VARCHAR(16),
  sys_ai_usage_billing_mode VARCHAR(16),
  sys_ai_usage_cached_tokens INT,
  sys_ai_usage_cached_cost DOUBLE,
  sys_ai_usage_input_price_per_1m DOUBLE,
  sys_ai_usage_output_price_per_1m DOUBLE,
  sys_ai_usage_cache_read_price_per_1m DOUBLE,
  sys_ai_usage_service_tier VARCHAR(32),
  sys_ai_usage_ratio DOUBLE,
  sys_ai_usage_original_cost DOUBLE,
  sys_ai_usage_first_token_latency BIGINT,
  sys_ai_usage_total_latency BIGINT,
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT
);

CREATE TABLE IF NOT EXISTS sys_ai_usage_daily (
  sys_ai_usage_daily_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_usage_daily_stat_date DATE,
  sys_ai_usage_daily_group_name VARCHAR(64),
  sys_ai_usage_daily_provider VARCHAR(64),
  sys_ai_usage_daily_model VARCHAR(128),
  sys_ai_usage_daily_total_calls BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_prompt_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_completion_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_cost DOUBLE DEFAULT 0,
  sys_ai_usage_daily_total_cached_tokens BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_latency BIGINT DEFAULT 0,
  sys_ai_usage_daily_total_first_token_latency BIGINT DEFAULT 0,
  sys_ai_usage_daily_standard_cost DOUBLE DEFAULT 0,
  create_name VARCHAR(64),
  create_by VARCHAR(64),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by VARCHAR(64),
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_pricing (
  sys_ai_pricing_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_pricing_provider VARCHAR(64),
  sys_ai_pricing_model VARCHAR(128),
  sys_ai_pricing_input_price_per_1k DOUBLE,
  sys_ai_pricing_output_price_per_1k DOUBLE,
  sys_ai_pricing_currency VARCHAR(8),
  sys_ai_pricing_effective_date TIMESTAMP,
  sys_ai_pricing_source VARCHAR(32),
  create_name VARCHAR(64),
  create_by INT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_name VARCHAR(64),
  update_by INT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_manufacturer (
  sys_ai_manufacturer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_name VARCHAR(64),
  sys_ai_manufacturer_code VARCHAR(64),
  sys_ai_manufacturer_status INT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 鐞涖儵缍堥崢鍌氭櫌鐎圭偘缍嬮幍鈧棁鈧崚妤嬬礄娑?SysAiManufacturer 鐎靛綊缍堥敍?ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_factory VARCHAR(255);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_base_url VARCHAR(512);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_app_key VARCHAR(255);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_api_secret VARCHAR(512);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_openai_compatible TINYINT DEFAULT 0;
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_logo VARCHAR(512);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_remark VARCHAR(512);
ALTER TABLE sys_ai_manufacturer ADD COLUMN IF NOT EXISTS sys_ai_manufacturer_sort INT DEFAULT 0;

CREATE TABLE IF NOT EXISTS sys_ai_manufacturer_key (
  sys_ai_manufacturer_key_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_key_manufacturer_id BIGINT,
  sys_ai_manufacturer_key_key VARCHAR(256),
  sys_ai_manufacturer_key_status INT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_model_setting (
  sys_ai_model_setting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_model_setting_bot_id VARCHAR(128),
  sys_ai_model_setting_field VARCHAR(128),
  sys_ai_model_setting_value CLOB,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_ai_app_key (
  sys_ai_app_key_id INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_app_key_app_id VARCHAR(128),
  sys_ai_app_key_app_key VARCHAR(128),
  sys_ai_app_key_secret VARCHAR(256),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 鏉╂粎鈻兼稉缁樻簚閿涘湯SH 娑撶粯婧€缁ㄥ尅绱氶垾?/api/ssh-hosts 鐠囪鍟撴笟婵婄濮濄倛銆?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_ssh_host (
  sys_ai_ssh_host_id             VARCHAR(36) NOT NULL PRIMARY KEY,
  sys_ai_ssh_host_name           VARCHAR(255),
  sys_ai_ssh_host_host           VARCHAR(255),
  sys_ai_ssh_host_port           INT DEFAULT 22,
  sys_ai_ssh_host_username       VARCHAR(128),
  sys_ai_ssh_host_auth_type      VARCHAR(32) DEFAULT 'password',
  sys_ai_ssh_host_password       VARCHAR(1024),
  sys_ai_ssh_host_private_key    CLOB,
  sys_ai_ssh_host_passphrase     VARCHAR(1024),
  sys_ai_ssh_host_certificate    CLOB,
  sys_ai_ssh_host_sudo_password  VARCHAR(1024),
  sys_ai_ssh_host_host_fingerprint VARCHAR(512),
  sys_ai_ssh_host_status         VARCHAR(32) DEFAULT 'unknown',
  sys_ai_ssh_host_last_connected_at BIGINT,
  sys_ai_ssh_host_created_at     BIGINT NOT NULL,
  sys_ai_ssh_host_updated_at     BIGINT NOT NULL
);

-- -------------------------------------------------------------------
-- AI Agent 鐎规矮绠?閳?/v2/ai/agents 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_agent (
  sys_ai_agent_id              VARCHAR(64) NOT NULL PRIMARY KEY,
  sys_ai_agent_parent_id       VARCHAR(64),
  sys_ai_agent_name            VARCHAR(128) NOT NULL,
  sys_ai_agent_description     VARCHAR(512),
  sys_ai_agent_role            VARCHAR(255),
  sys_ai_agent_instruction     CLOB,
  sys_ai_agent_system_prompt   CLOB,
  sys_ai_agent_provider_id     VARCHAR(64),
  sys_ai_agent_model           VARCHAR(255),
  sys_ai_agent_mcp_ids         CLOB,
  sys_ai_agent_skill_ids       CLOB,
  sys_ai_agent_strategy        CLOB,
  sys_ai_agent_enabled         TINYINT DEFAULT 1,
  sys_ai_agent_sort_order      INT DEFAULT 0,
  sys_ai_agent_create_time     BIGINT NOT NULL,
  sys_ai_agent_update_time     BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_ai_agent_parent ON sys_ai_agent(sys_ai_agent_parent_id);

-- -------------------------------------------------------------------
-- MCP 閺堝秴濮熼崳銊ョ暰娑?閳?/v2/mcp/* 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_mcp_server (
  sys_ai_mcp_server_id          VARCHAR(36) NOT NULL PRIMARY KEY,
  sys_ai_mcp_server_name        VARCHAR(255) NOT NULL,
  sys_ai_mcp_server_description VARCHAR(512),
  sys_ai_mcp_server_transport   CLOB NOT NULL,
  sys_ai_mcp_server_original_json CLOB,
  sys_ai_mcp_server_builtin    TINYINT DEFAULT 0,
  sys_ai_mcp_server_enabled    TINYINT DEFAULT 1,
  sys_ai_mcp_server_last_test_status VARCHAR(32),
  sys_ai_mcp_server_last_connected_at BIGINT,
  sys_ai_mcp_server_created_at BIGINT,
  sys_ai_mcp_server_updated_at  BIGINT
);

-- -------------------------------------------------------------------
-- 閻劑鍣洪弮銉ョ箶/濞村繗顫嶉崢鍡楀蕉 閳?Usage/Session 閻╃鍙ч幒銉ュ經娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_bot_message (
  sys_ai_bot_message_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_bot_message_bot_id   VARCHAR(128),
  sys_ai_bot_message_direction VARCHAR(16),
  sys_ai_bot_message_from_user VARCHAR(128),
  sys_ai_bot_message_content  CLOB,
  sys_ai_bot_message_create_time TIMESTAMP
);

-- -------------------------------------------------------------------
-- OCR 閸掑棛绮嶆妯款吇闁板秶鐤嗘い鐧哥礄娑?OcrGlobalSettingProvider 鐎靛綊缍堥敍灞肩返濠曟梻銇?閼辨棁鐨熼敍?
-- 娴ｈ法鏁?MERGE 娣囨繆鐦夐獮鍌滅搼閿涙岸鍣告径宥呮儙閸斻劋绗夌憴锕€褰傞崬顖欑闁款喖鍟跨粣?
-- -------------------------------------------------------------------
MERGE INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status) KEY(sys_ai_global_setting_group, sys_ai_global_setting_field) VALUES
('ocr','detector_model','paddleocrv6-det','STRING','閺傚洤鐡уΛ鈧ù瀣侀崹瀣剁礄paddleocrv6-det / paddleocrv6-medium-det閿?,1,1),
('ocr','recognizer_model','paddleocrv6-rec','STRING','閺傚洤鐡х拠鍡楀焼濡€崇€烽敍鍧ddleocrv6-rec / paddleocrv6-medium-rec / svtr-extractor / paddle-ocr-recognition / pp-word-extractor閿?,2,1),
('ocr','detector_threshold','0.5','NUMBER','OCR 濡偓濞村鐤嗘穱鈥冲闂冨牆鈧》绱欐潻鍥ㄦ姢娴ｅ海鐤嗘穱鈥冲閺傚洦婀板鍡礉0~1閿?,3,1),
('ocr','sort_reading_order','true','BOOLEAN','閺勵垰鎯侀幐澶愭鐠囧銆庢惔蹇斿笓鎼村骏绱欓崗?y 閸?x閿?,4,1),
('ocr','direction_model','','STRING','閺傜懓鎮滈惌顐ｎ劀濡€崇€烽敍鍧-word-rotate閿涘瞼鏆€缁屽搫鍙ч梻顓ㄧ礆',5,1),
('ocr','restorer_model','','STRING','閺傚洤鐡х搾鍛瀻娣囶喖顦插Ο鈥崇€烽敍鍧眅xt-bsr閿涘瞼鏆€缁屽搫鍙ч梻顓ㄧ礆',6,1),
('ocr','device','cpu','STRING','閹恒劎鎮婄拋鎯ь槵閿涘潏pu / cuda閿?,7,1);

-- -------------------------------------------------------------------
-- 濡€崇€风粻锛勬倞 閳?/v2/models 娓氭繆绂嗗銈堛€冮敍鍫濇儓閸掝偄鎮?aliases閵嗕礁娆㈡潻?last_latency閿?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_model (
  sys_ai_model_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_model_name            VARCHAR(255),
  sys_ai_model_code            VARCHAR(128),
  sys_ai_model_provider_id     BIGINT,
  sys_ai_model_capabilities    CLOB,
  sys_ai_model_description     VARCHAR(512),
  sys_ai_model_enabled         TINYINT DEFAULT 1,
  sys_ai_model_sort            INT DEFAULT 0,
  sys_ai_model_create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_model_update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_model_deleted         INT DEFAULT 0,
  sys_ai_model_required_pipelines CLOB,
  sys_ai_model_optional_pipelines CLOB,
  sys_ai_model_input_price     DECIMAL(18,8),
  sys_ai_model_output_price    DECIMAL(18,8),
  sys_ai_model_cache_price      DECIMAL(18,8),
  sys_ai_model_aliases          CLOB,
  sys_ai_model_last_latency     BIGINT,
  sys_ai_model_last_test_time  TIMESTAMP
);

-- -------------------------------------------------------------------
-- 鐟欐帟澹婄粻锛勬倞 閳?/v2/roles閵嗕阜oleOfflineProvider 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_role (
  sys_ai_role_id            INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_role_name          VARCHAR(255),
  sys_ai_role_code          VARCHAR(128),
  sys_ai_role_description   VARCHAR(512),
  sys_ai_role_system_prompt CLOB,
  sys_ai_role_model         VARCHAR(128),
  sys_ai_role_temperature   DOUBLE,
  sys_ai_role_sort          INT DEFAULT 0,
  sys_ai_role_status        INT DEFAULT 1,
  sys_ai_role_delete        INT DEFAULT 0,
  create_name               VARCHAR(255),
  create_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 鐟欐帟澹婇崚鍡欑矋缁狅紕鎮?閳?/v2/role-groups
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_role_group (
  sys_ai_role_group_id      INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_role_group_name    VARCHAR(255),
  sys_ai_role_group_sort    INT DEFAULT 0,
  sys_ai_role_group_status  INT DEFAULT 1,
  sys_ai_role_group_delete  INT DEFAULT 0,
  create_name               VARCHAR(255),
  create_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time               TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 濡€崇€锋笟娑樼安閸?閳?ChatClient 鐠侯垳鏁辨笟婵婄濮濄倛銆?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_provider (
  sys_ai_provider_id        VARCHAR(64) PRIMARY KEY,
  sys_ai_provider_name      VARCHAR(255),
  sys_ai_provider_type      VARCHAR(64),
  sys_ai_provider_base_url  VARCHAR(512),
  sys_ai_provider_api_key   VARCHAR(1024),
  sys_ai_provider_logo      VARCHAR(512),
  sys_ai_provider_models    CLOB,
  sys_ai_provider_enabled   TINYINT DEFAULT 1,
  sys_ai_provider_deleted   INT DEFAULT 0,
  sys_ai_provider_platform  VARCHAR(64),
  sys_ai_provider_protocol  VARCHAR(32),
  sys_ai_provider_sort_order INT DEFAULT 0,
  sys_ai_provider_attributes CLOB,
  sys_ai_provider_extra     CLOB,
  sys_ai_provider_proxy     VARCHAR(255),
  sys_ai_provider_create_time BIGINT,
  sys_ai_provider_update_time BIGINT
);

-- -------------------------------------------------------------------
-- 閹绘劒绶甸崯鍡樐侀崹?閳?/api/providers 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_provider_model (
  sys_ai_provider_model_provider_id    VARCHAR(64)  NOT NULL,
  sys_ai_provider_model_model          VARCHAR(255) NOT NULL,
  sys_ai_provider_model_enabled        TINYINT      DEFAULT 1,
  sys_ai_provider_model_sort_order     INT          DEFAULT 0,
  sys_ai_provider_model_description    CLOB,
  sys_ai_provider_model_capabilities   CLOB,
  sys_ai_provider_model_metrics        CLOB,
  sys_ai_provider_model_create_time    BIGINT,
  sys_ai_provider_model_update_time    BIGINT,
  PRIMARY KEY (sys_ai_provider_model_provider_id, sys_ai_provider_model_model)
);

-- -------------------------------------------------------------------
-- 閹绘劒绶甸崯鍡氱箾閹?閳?/api/providers 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_provider_connection (
  sys_ai_provider_connection_id         VARCHAR(64)  NOT NULL PRIMARY KEY,
  sys_ai_provider_connection_provider_id VARCHAR(64) NOT NULL,
  sys_ai_provider_connection_role       VARCHAR(64)  NOT NULL,
  sys_ai_provider_connection_label      VARCHAR(255),
  sys_ai_provider_connection_base_url   VARCHAR(512),
  sys_ai_provider_connection_auth_scheme VARCHAR(32),
  sys_ai_provider_connection_credentials CLOB,
  sys_ai_provider_connection_extra      CLOB,
  sys_ai_provider_connection_create_time BIGINT,
  sys_ai_provider_connection_update_time BIGINT
);
CREATE INDEX IF NOT EXISTS idx_ai_provider_conn_pid ON sys_ai_provider_connection(sys_ai_provider_connection_provider_id);

-- -------------------------------------------------------------------
-- 閺堝秴濮熸潻鎰攽閻樿埖鈧?閳?/v2/service-status 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_service_status (
  sys_ai_service_status_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_service_status_service_name VARCHAR(255),
  sys_ai_service_status_running TINYINT DEFAULT 0,
  sys_ai_service_status_pid     BIGINT,
  sys_ai_service_status_start_time TIMESTAMP,
  sys_ai_service_status_uptime_sec BIGINT,
  sys_ai_service_status_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 閸樺倸鏅㈤敍鍫濆煑闁姴鏅㈤敍澶嗏偓?/v2/models 娓氭繆绂嗗銈堛€?-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_ai_manufacturer (
  sys_ai_manufacturer_id            INT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_manufacturer_code          VARCHAR(128),
  sys_ai_manufacturer_name          VARCHAR(255),
  sys_ai_manufacturer_factory       VARCHAR(255),
  sys_ai_manufacturer_base_url      VARCHAR(512),
  sys_ai_manufacturer_app_key       VARCHAR(255),
  sys_ai_manufacturer_api_secret    VARCHAR(512),
  sys_ai_manufacturer_openai_compatible TINYINT DEFAULT 0,
  sys_ai_manufacturer_logo          VARCHAR(512),
  sys_ai_manufacturer_remark        VARCHAR(512),
  sys_ai_manufacturer_status        INT DEFAULT 1,
  sys_ai_manufacturer_sort          INT DEFAULT 0,
  create_time                       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time                       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 鐞涖儱鍘?sys_ai_provider 鐞涖劎宸辨径鍗炲灙閿涘牅绗?Mapper 鐎靛綊缍堥敍?-- -------------------------------------------------------------------
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_platform VARCHAR(128);
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_auth_scheme VARCHAR(64);
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_credentials CLOB;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_bedrock_config CLOB;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_extra CLOB;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_proxy VARCHAR(255);
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_proxy_id BIGINT;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_sort_order INT DEFAULT 0;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_code VARCHAR(128);
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_status INT DEFAULT 1;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE sys_ai_provider ADD COLUMN IF NOT EXISTS sys_ai_provider_update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
CREATE TABLE IF NOT EXISTS sys_ai_connector (
  sys_ai_connector_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sys_ai_connector_key VARCHAR(128),
  sys_ai_connector_name VARCHAR(128),
  sys_ai_connector_description VARCHAR(512),
  sys_ai_connector_type VARCHAR(64),
  sys_ai_connector_category VARCHAR(64),
  sys_ai_connector_icon VARCHAR(256),
  sys_ai_connector_endpoint CLOB,
  sys_ai_connector_params CLOB,
  sys_ai_connector_builtin TINYINT DEFAULT 0,
  sys_ai_connector_enabled TINYINT DEFAULT 1,
  sys_ai_connector_last_test_status VARCHAR(32),
  sys_ai_connector_last_tested_at TIMESTAMP,
  sys_ai_connector_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  sys_ai_connector_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------------------------------
-- 鏉╂劘顢戦弮?ChatClient 姒涙顓婚柊宥囩枂閿涙碍娅ょ拫?GLM glm-4-flash閿涘牆鍘ょ拹瑙勀侀崹瀣剁礆
-- -------------------------------------------------------------------
MERGE INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status) KEY(sys_ai_global_setting_group, sys_ai_global_setting_field) VALUES
('runtime','provider','openai','STRING','AI 閺堝秴濮熼崯鍡樼垼鐠?,1,1),
('runtime','provider_base_url','https://open.bigmodel.cn/api/paas/v4','STRING','API 閸╄櫣顢呴崷鏉挎絻',2,1),
('runtime','provider_api_key','29ed238f4660416cb2efe39aaaf71ed5.S2I2AfOu75HQwZ30','STRING','API 鐎靛棝鎸滈敍鍫熸鐠?GLM閿?,3,1),
('runtime','default_model','glm-4-flash','STRING','姒涙顓诲Ο鈥崇€烽敍鍧搇m-4-flash 閸忓秷鍨傞敍?,4,1),
('runtime','temperature','0.3','NUMBER','濞撯晛瀹?,5,1);

-- -------------------------------------------------------------------
-- RAG 閻儴鐦戞惔鎾舵埂鐎圭偞膩閸ㄥ鍘ょ純顕嗙礄LLM 閻劍娅ょ拫?GLM閿涙矠mbedding 閻?OpenRouter 閸忓秷鍨傚Ο鈥崇€烽敍?
-- -------------------------------------------------------------------
MERGE INTO sys_ai_global_setting (sys_ai_global_setting_group, sys_ai_global_setting_field, sys_ai_global_setting_value, sys_ai_global_setting_value_type, sys_ai_global_setting_description, sys_ai_global_setting_sort, sys_ai_global_setting_status) KEY(sys_ai_global_setting_group, sys_ai_global_setting_field) VALUES
('rag','rag_llm_model','glm-4-flash','STRING','RAG 闂傤喚鐡熸担璺ㄦ暏閻?LLM 濡€崇€烽崥?,3,1),
('rag','rag_embedding_model','openai','STRING','Embedding 濡€崇€烽敍鍧enai/SPI provider閿?,1,1),
('rag','rag_embedding_dimensions','2048','NUMBER','閸氭垿鍣虹紒鏉戝',2,1),
('rag','rag_embedding_base_url','https://openrouter.ai/api/v1','STRING','Embedding API 閸╄櫣顢呴崷鏉挎絻閿涘湦penRouter閿?,4,1),
('rag','rag_embedding_api_key','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','STRING','Embedding API 鐎靛棝鎸滈敍鍦enRouter閿?,5,1),
('rag','rag_embedding_model_name','nvidia/nemotron-3-embed-1b:free','STRING','Embedding 濡€崇€烽弽鍥槕閿涘牆顩?nvidia/nemotron-3-embed-1b:free閿?,6,1);

-- ===== sys_ai_requirement (需求条目表) =====
CREATE TABLE IF NOT EXISTS sys_ai_requirement (
  sys_ai_requirement_id VARCHAR(36) NOT NULL,
  sys_ai_requirement_display_no INT,
  sys_ai_requirement_title VARCHAR(512),
  sys_ai_requirement_content CLOB,
  sys_ai_requirement_tag VARCHAR(128),
  sys_ai_requirement_order_key VARCHAR(64),
  sys_ai_requirement_status VARCHAR(32) DEFAULT 'pending',
  sys_ai_requirement_completion_note VARCHAR(1024),
  sys_ai_requirement_owner_conversation_id VARCHAR(64),
  sys_ai_requirement_owner_terminal_id VARCHAR(64),
  sys_ai_requirement_origin VARCHAR(32),
  sys_ai_requirement_started_at BIGINT,
  sys_ai_requirement_completed_at BIGINT,
  sys_ai_requirement_attempt_count INT DEFAULT 0,
  sys_ai_requirement_extra CLOB,
  create_name VARCHAR(64),
  create_by INT,
  create_time BIGINT,
  update_name VARCHAR(64),
  update_by INT,
  update_time BIGINT,
  PRIMARY KEY (sys_ai_requirement_id)
);

-- ===== sys_ai_requirement 相关辅助表 =====
CREATE TABLE IF NOT EXISTS sys_ai_requirement_check (
  sys_ai_requirement_check_id VARCHAR(36) NOT NULL,
  sys_ai_requirement_check_requirement_id VARCHAR(36),
  sys_ai_requirement_check_status VARCHAR(32) DEFAULT 'pending',
  create_time BIGINT,
  update_time BIGINT,
  PRIMARY KEY (sys_ai_requirement_check_id)
);