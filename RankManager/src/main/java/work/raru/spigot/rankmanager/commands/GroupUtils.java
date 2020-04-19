package work.raru.spigot.rankmanager.commands;

import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.MutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.track.DemotionResult;
import net.luckperms.api.track.PromotionResult;
import net.luckperms.api.track.Track;
import work.raru.spigot.rankmanager.Main;
import work.raru.spigot.rankmanager.discord.RoleUtils;
import work.raru.spigot.rankmanager.discord.UserLinkManager;

public class GroupUtils {
	LuckPerms LPapi = Main.LPapi;
	int promote(String trackName, UUID UserUUID) {
		User user = LPapi.getUserManager().getUser(UserUUID);
		@Nullable
		Track track = LPapi.getTrackManager().getTrack(trackName);
		@NonNull
		MutableContextSet context = LPapi.getContextManager().getContextSetFactory().mutable();
		@NonNull PromotionResult result = track.promote(user, context);
		if (!result.getStatus().wasSuccessful()) {
			return 2;
		}
		Long discordId = UserLinkManager.getDiscordId(UserUUID);
		if (discordId == null) {
			return 1;
		}
		RoleUtils ru = new RoleUtils();
		if (result.getGroupFrom().isPresent()) {
			ru.removeRole(discordId, result.getGroupFrom().get());
		}
		if (result.getGroupTo().isPresent()) {
			ru.addRole(discordId, result.getGroupTo().get());
		}
		LPapi.getUserManager().saveUser(user);
		return 0;
	}
	int demote(String trackName, UUID UserUUID) {
		User user = LPapi.getUserManager().getUser(UserUUID);
		@Nullable
		Track track = LPapi.getTrackManager().getTrack(trackName);
		@NonNull
		MutableContextSet context = LPapi.getContextManager().getContextSetFactory().mutable();
		@NonNull DemotionResult result = track.demote(user, context);
		if (!result.getStatus().wasSuccessful()) {
			return 2;
		}
		Long discordId = UserLinkManager.getDiscordId(UserUUID);
		if (discordId == null) {
			return 1;
		}
		RoleUtils ru = new RoleUtils();
		if (result.getGroupFrom().isPresent()) {
			ru.removeRole(discordId, result.getGroupFrom().get());
		}
		if (result.getGroupTo().isPresent()) {
			ru.addRole(discordId, result.getGroupTo().get());
		}
		LPapi.getUserManager().saveUser(user);
		return 0;
	}
}
