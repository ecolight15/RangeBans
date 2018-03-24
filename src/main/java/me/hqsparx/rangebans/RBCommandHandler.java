package me.hqSparx.RangeBans;

import org.bukkit.command.CommandSender;

public class RBCommandHandler {

    public static RangeBans plugin;
    public static String regex = "[0-9\\.\\-\\*]*";
    private boolean lastresult = true;
    int PER_PAGE = 10;

    public RBCommandHandler(RangeBans instance) {
        plugin = instance;
    }

    public byte checkByte(String word) {
        if (Integer.parseInt(word) > 127) {
            return (byte) (Integer.parseInt(word) - 256);
        } else {
            return Byte.parseByte(word);
        }
    }

    public boolean command(CommandSender sender, String[] args) {

        if ((args.length == 0 || args.length > 4)) {
            if (checkPerm(sender, "rb.help")) {
                plugin.strings.sendHelp(sender);
            }
            return true;
        }

        int len = args.length;
        String label = args[0];

        if (label.equalsIgnoreCase("reload") && checkPerm(sender, "rb.reload")) {
            plugin.doReload(sender);
            return true;
        }
        if (label.equalsIgnoreCase("ban") && checkPerm(sender, "rb.ban") && len >= 2) {
            ban(sender, args);
            return true;
        }
        if (label.equalsIgnoreCase("unban") && checkPerm(sender, "rb.ban") && len >= 2) {
            unban(sender, args);
            return true;
        }
        if (label.equalsIgnoreCase("banhost") && checkPerm(sender, "rb.ban") && len == 2) {
            banhost(sender, args[1]);
            return true;
        }
        if (label.equalsIgnoreCase("unbanhost") && checkPerm(sender, "rb.ban") && len == 2) {
            unbanhost(sender, args[1]);
            return true;
        }
        if (label.equalsIgnoreCase("exception") && checkPerm(sender, "rb.exception") && len == 2) {
            exception(sender, args[1]);
            return true;
        }
        if (label.startsWith("removeex") && checkPerm(sender, "rb.exception") && len == 2) {
            removeexception(sender, args[1]);
            return true;
        }
        if (label.equalsIgnoreCase("ip") && checkPerm(sender, "rb.check") && len == 2) {
            checkip(sender, args[1]);
            return true;
        }
        if (label.equalsIgnoreCase("listbans") && checkPerm(sender, "rb.list") && len <= 2) {
            String page = "";
            page = (args.length > 1) ? args[1] : "1";
            showBanList(sender, page);
            return true;
        }
        if (label.equalsIgnoreCase("listhosts") && checkPerm(sender, "rb.list") && len <= 2) {
            String page = "";
            page = (args.length > 1) ? args[1] : "1";
            showHostList(sender, page);
            return true;
        }
        if (label.startsWith("listex") && checkPerm(sender, "rb.list") && len <= 2) {
            String page = "";
            page = (args.length > 1) ? args[1] : "1";
            showExceptionList(sender, page);
            return true;
        }

        if (lastresult) {
            plugin.strings.msg(sender, "Oops! Wrong syntax, check /rb for help.");
        }

        return true;
    }

    public String mergeargs(String[] tomerge) {
        String output = "";
        for (int i = 1; i < tomerge.length; i++) {
            output += tomerge[i].trim();
        }

        return output;
    }

    public RBIPFields checkIP(String ip) {
        ip = ip.replace(" ", "");
        byte[] min = new byte[4];
        byte[] max = new byte[4];
        String[] split = ip.split("\\.");
        String ip2 = "";

        if (split.length > 5) {
            String[] separated = ip.split("\\-");
            String[] one = separated[0].split("\\.");
            String[] two = separated[1].split("\\.");
            String toip1 = "";
            String toip2 = "";
            for (int i = 0; i < 4; i++) {
                min[i] = checkByte(one[i]);
                max[i] = checkByte(two[i]);
                toip1 += one[i];
                toip2 += two[i];
                if (i < 3) {
                    toip1 += ".";
                    toip2 += ".";
                }
            }
            ip2 = toip1 + " - " + toip2;
        } else {
            for (int i = 0; i < 4; i++) {
                if (split.length - 1 < i) {
                    min[i] = 0;
                    max[i] = -1;
                    ip2 += "*";
                } else if (split[i] == null || split[i].contentEquals("*")
                        || split[i].contentEquals("")) {
                    min[i] = 0;
                    max[i] = -1;
                    ip2 += "*";
                } else if (split[i].contains("-")) {
                    String[] split2 = split[i].split("\\-");
                    min[i] = checkByte(split2[0]);
                    max[i] = checkByte(split2[1]);
                    ip2 += split2[0] + "-" + split2[1];
                } else {
                    min[i] = checkByte(split[i]);
                    max[i] = checkByte(split[i]);
                    ip2 += split[i];
                }

                if (i < 3) {
                    ip2 += ".";
                }
            }
        }
        /*
		plugin.logger.info(min[0]+"-"+max[0]+"."+min[1]+"-"+max[1]+"."+
				min[2]+"-"+max[2]+"."+min[3]+"-"+max[3]);
         */
        return new RBIPFields(min, max, ip2);
    }

    public boolean checkPerm(CommandSender sender, String permission) {
        if (sender.hasPermission(permission) || sender.hasPermission("rb.*")) {
            lastresult = true;
            return true;
        } else {
            plugin.strings.msg(sender, "Sorry, you cannot access this command.");
            lastresult = false;
            return false;
        }
    }

    public void ban(CommandSender sender, String[] args) {
        String ip = mergeargs(args);
        if (ip.matches(regex)) {
            RBIPFields range = checkIP(ip);
            if (plugin.add(range)) {
                try {
                    plugin.saveLists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                plugin.strings.msg(sender, "&eBanning IP range: " + range.Address);
                plugin.logger.info(sender.getName() + " banned IP range: " + range.Address);
            } else {
                plugin.strings.msg(sender, "&cFailed to ban: " + range.Address);
            }
        } else {
            plugin.strings.msg(sender, "&cWrong IP range, check syntax");
        }
    }

    public void unban(CommandSender sender, String[] args) {
        String ip = mergeargs(args);
        if (ip.matches(regex)) {
            RBIPFields range = checkIP(ip);
            if (plugin.remove(range)) {
                try {
                    plugin.saveLists();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                plugin.strings.msg(sender, "&eUnbanning IP range: " + range.Address);
                plugin.logger.info(sender.getName() + " unbanned IP range: " + range.Address);
            } else {
                plugin.strings.msg(sender, "&cFailed to unban: " + range.Address);
            }
        } else {
            plugin.strings.msg(sender, "&cWrong IP range, check syntax");
        }
    }

    public void banhost(CommandSender sender, String host) {
        if (plugin.addhostname(host)) {
            try {
                plugin.saveLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.strings.msg(sender, "&eBanning hostname: " + host);
            plugin.logger.info(sender.getName() + " banned hostname: " + host);
        } else {
            plugin.strings.msg(sender, "&cFailed to ban: " + host);
        }
    }

    public void unbanhost(CommandSender sender, String host) {
        if (plugin.removehostname(host)) {
            try {
                plugin.saveLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.strings.msg(sender, "&eUnbanning hostname: " + host);
            plugin.logger.info(sender.getName() + " unbanned hostname: " + host);
        } else {
            plugin.strings.msg(sender, "&cFailed to unban: " + host);
        }
    }

    public void exception(CommandSender sender, String name) {
        if (plugin.addexception(name)) {
            try {
                plugin.saveLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.strings.msg(sender, ("&eAdding nickname exception: " + name));
            plugin.logger.info(sender.getName() + " added nickname exception: " + name);
        } else {
            plugin.strings.msg(sender, ("&cFailed to add nickname exception: " + name));
        }
    }

    public void removeexception(CommandSender sender, String name) {
        if (plugin.removeexception(name)) {
            try {
                plugin.saveLists();
            } catch (Exception e) {
                e.printStackTrace();
            }
            plugin.strings.msg(sender, ("&eRemoving nickname exception: " + name));
            plugin.logger.info(sender.getName() + " removed nickname exception: " + name);
        } else {
            plugin.strings.msg(sender, ("&cFailed to remove nickname exception: " + name));
        }
    }

    public void checkip(CommandSender sender, String name) {
        String ip = "";
        try {
            ip = plugin.getServer().getPlayer(name).getAddress().getAddress().getHostAddress();
            plugin.strings.msg(sender, ("&7" + plugin.getServer().getPlayer(name).getName() + "'s IP: &a" + ip));
        } catch (Exception e) {
            if (ip == null || ip == "") {
                plugin.strings.msg(sender, ("&cFailed to load player's IP: " + name));
            }
        }
    }

    private boolean checkDigit(String check) {
        for (int i = 0; i < check.length(); ++i) {
            if (!Character.isDigit(check.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void showBanList(CommandSender sender, String pagestr) {
        if (!checkDigit(pagestr)) {
            plugin.strings.msg(sender, "Only positive digits!");
            return;
        }

        int page = Integer.parseInt(pagestr);

        int pos = this.PER_PAGE * (page - 1);
        int size = plugin.bansSize();

        if (page > size / PER_PAGE + 1) {
            page = size / PER_PAGE + 1;
            pos = size - size % PER_PAGE;
        } else if (page == 0) {
            pos = 0;
            page = 1;
        }

        String header = "";
        header = "&6Bans list (page " + page + "/" + (size / PER_PAGE + 1) + ")";
        plugin.strings.msg(sender, header);

        if (size == 0) {
            plugin.strings.msg(sender, "&7There are no entries.");
        } else {
            for (int i = pos; i < pos + PER_PAGE; i++) {
                String line = "";
                if (i < size) {
                    line = "&7#" + (i + 1) + " &a" + plugin.getBan(i);
                    plugin.strings.msg(sender, line);
                }
            }
        }
    }

    public void showExceptionList(CommandSender sender, String pagestr) {
        if (!checkDigit(pagestr)) {
            plugin.strings.msg(sender, "Only positive digits!");
            return;
        }

        int page = Integer.parseInt(pagestr);
        int pos = this.PER_PAGE * (page - 1);
        int size = plugin.exceptionsSize();

        if (page > size / PER_PAGE + 1) {
            page = size / PER_PAGE + 1;
            pos = size - size % PER_PAGE;
        } else if (page == 0) {
            pos = 0;
            page = 1;
        }

        String header = "";
        header = "&6Exceptions list (page " + page + "/" + (size / PER_PAGE + 1) + ")";
        plugin.strings.msg(sender, header);

        if (size == 0) {
            plugin.strings.msg(sender, "&7There are no entries.");
        } else {
            for (int i = pos; i < pos + PER_PAGE; i++) {
                String line = "";
                if (i < size) {
                    line = "&7#" + (i + 1) + " &a" + plugin.getException(i);
                    plugin.strings.msg(sender, line);
                }
            }
        }

    }

    public void showHostList(CommandSender sender, String pagestr) {
        if (!checkDigit(pagestr)) {
            plugin.strings.msg(sender, "Only positive digits!");
            return;
        }

        int page = Integer.parseInt(pagestr);
        int pos = this.PER_PAGE * (page - 1);
        int size = plugin.hostsSize();

        if (page > size / PER_PAGE + 1) {
            page = size / PER_PAGE + 1;
            pos = size - size % PER_PAGE;
        } else if (page == 0) {
            pos = 0;
            page = 1;
        }

        String header = "";
        header = "&6Hostname bans list (page " + page + "/" + (size / PER_PAGE + 1) + ")";
        plugin.strings.msg(sender, header);

        if (size == 0) {
            plugin.strings.msg(sender, "&7There are no entries.");
        } else {
            for (int i = pos; i < pos + PER_PAGE; i++) {
                String line = "";
                if (i < size) {
                    line = "&7#" + (i + 1) + " &a" + plugin.getHost(i);
                    plugin.strings.msg(sender, line);
                }
            }
        }

    }

}
